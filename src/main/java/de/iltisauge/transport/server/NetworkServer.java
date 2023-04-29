package de.iltisauge.transport.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.*;
import java.util.stream.Collectors;

import de.iltisauge.transport.Transport;
import de.iltisauge.transport.client.NetworkClient;
import de.iltisauge.transport.messages.HandleSubscriptionsMessage;
import de.iltisauge.transport.messages.HandleSubscriptionsMessage.HandleSubscriptionType;
import de.iltisauge.transport.messages.TextMessage;
import de.iltisauge.transport.network.*;
import de.iltisauge.transport.utils.ConsoleLoggingFormatter;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * This class represents a NetworkServer that is used to make communication between multiple {@link NetworkClient}s possible.
 * 
 * @author Daniel Ziegler
 *
 */
@RequiredArgsConstructor
@Getter
public class NetworkServer extends NetworkDevice {
	
	/**
	 * This method can be used to test the NetworkServer.
	 * @param args
	 */
	public static void main(String[] args) {
		final Logger logger = Logger.getLogger("transport");
		logger.addHandler(new ConsoleLoggingFormatter());
		logger.setUseParentHandlers(false);
		Transport.setLogger(logger);
		final SubscriptionManager subscriptionManager = new SubscriptionManager();
		final String address = System.getProperty("server-address", "127.0.0.1");
		final Integer port = Integer.valueOf(System.getProperty("server-port", "8917"));
		final NetworkServer server = new NetworkServer(new ServerNetworkManager(subscriptionManager), subscriptionManager, new InetSocketAddress(address, port));
		server.initialize();
		if (!server.start(true)) {
			logger.log(Level.SEVERE, "Could not start NetworkServer.");
			System.exit(0);
			return;
		}
		new Thread(() -> {
			final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			String line = null;
			try {
				while ((line = reader.readLine()) != null) {
					if (line.startsWith("stop")) {
						System.exit(0);
					}
				}
			} catch (IOException exception) {
				exception.printStackTrace();
			}
		}).start();
	}

	@Getter
	private final ServerNetworkManager networkManager;
	private final SubscriptionManager subcriptionManager;
	/**
	 * Represents the address that the server will be bound to.
	 */
	private final SocketAddress address;
	private NioEventLoopGroup bossGroup = null;
	private NioEventLoopGroup workerGroup = null;
	private ServerBootstrap serverBootstrap = null;
	private boolean isRunning;

	/**
	 * Initializes the NetworkServer.<br>
	 * This method also registers the default message codecs. See {@link NetworkManager#registerDefaultCodecs()}.
	 */
	@Override
	public void initialize() {
		Transport.setNetworkManager(networkManager);
		networkManager.registerDefaultCodecs();
		bossGroup = new NioEventLoopGroup();
		workerGroup = new NioEventLoopGroup();
		serverBootstrap = new ServerBootstrap();
		serverBootstrap.group(bossGroup, workerGroup);
		serverBootstrap.channel(NioServerSocketChannel.class);
		serverBootstrap.option(ChannelOption.TCP_NODELAY, true).option(ChannelOption.SO_KEEPALIVE, true);
		serverBootstrap.childOption(ChannelOption.TCP_NODELAY, true).option(ChannelOption.SO_KEEPALIVE, true);
		final ChannelInitializer channelInitializer = new ChannelInitializer(this);
		serverBootstrap.handler(channelInitializer);
		serverBootstrap.childHandler(channelInitializer);
	}

	/**
	 * This method starts the NetworkServer.
	 * Decide whether you want to log all in-/outbound packets and connections.
	 * @param logTraffic
	 * @return true, if the server has been successfully bound to the given address, otherwise false.
	 */
	@Override
	public boolean start(boolean logTraffic) {
		Transport.getLogger().log(Level.INFO, "Starting up NetworkServer on " + address.toString() + "...");
		setLogTraffic(logTraffic);
		Transport.getLogger().log(Level.INFO, (logTraffic ? "Logging" : "Not logging") + " traffic");
		Runtime.getRuntime().addShutdownHook(new Thread(() -> shutdown()));
		registerEvents();
		try {
			return serverBootstrap.bind(address).syncUninterruptibly().addListener(future -> {
				if (future.cause() != null) {
					future.cause().printStackTrace();
				}
				isRunning = true;
				onStarted();
			}).syncUninterruptibly().isSuccess();
		} catch(Exception exception) {
			Transport.getLogger().log(Level.WARNING, "An error occurred while binding the network server to " + address.toString(), exception);
		}
		return false;
	}

	private void registerEvents() {
		if (isLogTraffic()) {
			networkManager.registerEvent(new IMessageEvent<IMessage>() {

				@Override
				public void onReceived(IMessage message) {
					Transport.getLogger().log(Level.INFO, "[<-] " + message.getClass().getName() + " Channels: " + Arrays.asList(message.getChannels()).stream().collect(Collectors.joining(", ")));
				}

				@Override
				public void onSent(IMessage message) {
					Transport.getLogger().log(Level.INFO, "[->] " + message.getClass().getName() + " Channels: " + Arrays.asList(message.getChannels()).stream().collect(Collectors.joining(", ")));
				}
			});
		}
		networkManager.registerEvent(HandleSubscriptionsMessage.class, new IMessageEvent<HandleSubscriptionsMessage>() {

			@Override
			public void onReceived(HandleSubscriptionsMessage message) {
				final HandleSubscriptionType handleSubscriptionType = message.getHandleSubscriptionType();
				if (handleSubscriptionType.equals(HandleSubscriptionType.ADD)) {
					subcriptionManager.addSubscriptions(message.getFrom(), message.getChannelsToSubscribe());
				} else if (handleSubscriptionType.equals(HandleSubscriptionType.REMOVE)) {
					subcriptionManager.removeSubscriptions(message.getFrom(), message.getChannelsToSubscribe());
				}
			}
		});
	}

	/**
	 * This method is called when the server has been binded to the specific address.
	 */
	@Override
	public void onStarted() {
		Transport.getLogger().log(Level.INFO, "Successfully binded NetworkServer to " + address.toString());
	}
	
	/**
	 * Sends the {@link IMessage} to all connected clients.
	 * @param message
	 */
	public void broadcastMessage(IMessage message) {
		for (ISession session : networkManager.getSessions()) {
			session.send(message);
		}
	}

	/**
	 * Forwards the {@link ServerMessageWrapper} to all clients that have subscribed the channels of the message.
	 * @param message
	 */
	public void forwardMessage(ServerMessageWrapper message) {
		final Map<ISession, Set<String>> subscriptions = subcriptionManager.getSubscriptions();
		for (String channel : message.getChannels()) {
			for (Entry<ISession, Set<String>> entry : subscriptions.entrySet()) {
				if (!entry.getValue().contains(channel)) {
					continue;
				}
				if (entry.getKey().equals(message.getFrom().getChannel()) && !message.isReceiveSelf()) {
					continue;
				}
				entry.getKey().send(message, isLogTraffic());
			}
		}
	}

	/**
	 * This method shuts down the worker {@link NioEventLoopGroup} and boss {@link NioEventLoopGroup} and calls {@link #onShutdown()}.
	 */
	@Override
	public void shutdown() {
		workerGroup.shutdownGracefully();
		bossGroup.shutdownGracefully();
		isRunning = false;
		onShutdown();
	}
	
	/**
	 * This method is called when {@link #shutdown()} has been executed.
	 */
	@Override
	public void onShutdown() {
		Transport.getLogger().log(Level.INFO, "Successfully shut down NetworkServer on " + address.toString());
	}

	/**
	 * Sets the {@link NioEventLoopGroup}s and {@link Bootstrap} object to null.<br>
	 * Calls {@link #shutdown()} if the method was not called.
	 */
	@Override
	public void destroy() {
		if (isRunning()) {
			shutdown();
		}
		bossGroup = null;
		workerGroup = null;
		serverBootstrap = null;
	}
}
