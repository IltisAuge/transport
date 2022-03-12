package de.iltisauge.transport.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.iltisauge.transport.Transport;
import de.iltisauge.transport.messages.TextMessage;
import de.iltisauge.transport.network.ChannelInitializer;
import de.iltisauge.transport.network.IMessage;
import de.iltisauge.transport.network.IMessageEvent;
import de.iltisauge.transport.network.ISession;
import de.iltisauge.transport.network.NetworkDevice;
import de.iltisauge.transport.network.NetworkManager;
import de.iltisauge.transport.server.NetworkServer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * This class represents a {@link NetworkClient} and extends {@link NetworkDevice}.
 * It connects to the {@link NetworkServer} to allow communication between the server and other {@link NetworkClient}s.
 * 
 * @author Daniel Ziegler
 *
 */
@RequiredArgsConstructor
@Getter
public class NetworkClient extends NetworkDevice {
	
	/**
	 * This method can be used to test the connection to the server by sending {@link TextMessage}es to the server and receiving them back.
	 * @param args
	 */
	public static void main(String[] args) {
		final Logger logger = Logger.getLogger("transport");
		Transport.setLogger(logger);
		final String address = System.getProperty("server-address", "127.0.0.1");
		final Integer port = Integer.valueOf(System.getProperty("server-port", "8917"));
		final NetworkClient client = new NetworkClient(new ClientNetworkManager(), new InetSocketAddress(address, port));
		Transport.setClient(client);
		client.initialize();
		if (!client.start(true)) {
			logger.log(Level.SEVERE, "Could not start NetworkClient.");
			System.exit(0);
			return;
		}
		Transport.getNetworkManager().registerCodec(TextMessage.class, TextMessage.CODEC);
		Transport.getNetworkManager().registerEvent(TextMessage.class, new IMessageEvent<TextMessage>() {
			
			@Override
			public void onReceived(TextMessage message) {
				System.out.println("Received TextMessage: " + message.getText());
			}
			
			@Override
			public void onSent(TextMessage message) {
				System.out.println("Sent TextMessage: " + message.getText());
			}
		});
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
				String line = null;
				try {
					while ((line = reader.readLine()) != null) {
						if (line.startsWith("stop")) {
							System.exit(0);
						} else {
							new TextMessage(line).send("text-message");
						}
					}
				} catch (IOException exception) {
					exception.printStackTrace();
				}
			}
		}).start();
	}

	@Getter
	private final ClientNetworkManager networkManager;
	/**
	 * Represents the address of the server that the client tires to connect to.
	 */
	private final SocketAddress address;
	private NioEventLoopGroup eventLoopGroup = null;
	private Bootstrap bootstrap = null;
	private boolean isRunning;
	private ISession session;
	
	/**
	 * Initializes the NetworkClient.<br>
	 * This method also registers the default message codecs. See {@link NetworkManager#registerDefaultCodecs()}.
	 */
	@Override
	public void initialize() {
		networkManager.registerDefaultCodecs();
		eventLoopGroup = new NioEventLoopGroup();
		bootstrap = new Bootstrap();
		bootstrap.group(eventLoopGroup);
		bootstrap.channel(NioSocketChannel.class);
		bootstrap.option(ChannelOption.TCP_NODELAY, true).option(ChannelOption.SO_KEEPALIVE, true);
		final ChannelInitializer channelInitializer = new ChannelInitializer(this);
		bootstrap.handler(channelInitializer);
	}
	
	/**
	 * Decide whether you want to log all in-/outbound packets and connections.
	 * @param logTraffic
	 * @return true, if the client has successfully connected to the server on the given address.
	 */
	@Override
	public boolean start(boolean logTraffic) {
		Transport.getLogger().log(Level.INFO, "Connecting to NetworkServer on " + address.toString() + "...");
		setLogTraffic(logTraffic);
		Transport.getLogger().log(Level.INFO, (logTraffic ? "Logging" : "Not logging") + " traffic");
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			
			@Override
			public void run() {
				if (!isConnected()) {
					return;
				}
				shutdown();
			}
		}));
		try {
			return bootstrap.connect(address).syncUninterruptibly().addListener(new GenericFutureListener<Future<? super Void>>() {
				
				public void operationComplete(Future<? super Void> future) throws Exception {
					if (future.cause() != null) {
						future.cause().printStackTrace();
						return;
					}
					isRunning = true;
					onStarted();
				};
			}).isSuccess();
		} catch (Exception exception) {
			if (exception instanceof ConnectException && exception.getMessage().contains("Connection refused: no further information")) {
				Transport.getLogger().log(Level.WARNING, "The NetworkServer is unreachable! Please try again later!");
				return false;
			}
			Transport.getLogger().log(Level.WARNING, "An error occurred while connecting to the network server on " + address.toString(), exception);
		}
		return false;
	}
	
	/**
	 * This method is called when the client has established a connection to the server.
	 */
	@Override
	public void onStarted() {
		Transport.getLogger().log(Level.INFO, "Successfully connected to NetworkServer on " + address.toString());
	}
	
	/**
	 * 
	 * @return <code>true</code>, if the {@link ISession} object is null, otherwise <code>false</code>.
	 */
	public boolean isConnected() {
		return session != null && !eventLoopGroup.isShutdown() && !eventLoopGroup.isTerminated();
	}
	
	/**
	 * Sets the {@link ISession} object.
	 * @param session
	 */
	public void setSession(ISession session) {
		this.session = session;
	}

	/**
	 * Sends a {@link IMessage} to all given channels.<br>
	 * <b>You do not need to add the channels to the message object!</b>
	 * @param message
	 * @param channels
	 * @return true, if the message has been sent successfully, otherwise false.
	 */
	public boolean send(IMessage message, String... channels) {
		message.addChannels(channels);
		return session.send(message);
	}
	
	/**
	 * Shuts down the {@link NioEventLoopGroup} and calls {@link #onShutdown()}.
	 */
	@Override
	public void shutdown() {
		eventLoopGroup.shutdownGracefully();
		isRunning = false;
		onShutdown();
	}
	
	/**
	 * This method is called when {@link #shutdown()} has been executed.
	 */
	@Override
	public void onShutdown() {
		Transport.getLogger().log(Level.INFO, "Successfully disconnected from NetworkServer on " + address.toString());
	}

	/**
	 * Sets the {@link NioEventLoopGroup} and {@link Bootstrap} object to null.<br>
	 * Calls {@link #shutdown()} if the method was not called.
	 */
	@Override
	public void destroy() {
		if (isRunning()) {
			shutdown();
		}
		eventLoopGroup = null;
		bootstrap = null;
	}
}
