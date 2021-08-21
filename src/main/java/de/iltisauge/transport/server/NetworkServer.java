package de.iltisauge.transport.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import de.iltisauge.transport.Transport;
import de.iltisauge.transport.messages.HandleSubscriptionsMessage;
import de.iltisauge.transport.messages.HandleSubscriptionsMessage.HandleSubscriptionType;
import de.iltisauge.transport.network.ChannelInitializer;
import de.iltisauge.transport.network.IMessage;
import de.iltisauge.transport.network.IMessageEvent;
import de.iltisauge.transport.network.ISession;
import de.iltisauge.transport.network.NetworkDevice;
import de.iltisauge.transport.network.NetworkManager;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class NetworkServer {
	
	public static void main(String[] args) {
		final NetworkServer server = new NetworkServer(new SubcriptionManager(), new InetSocketAddress("127.0.0.1", 5001));
		server.initialize();
		server.start();
	}

	private final SubcriptionManager subcriptionManager;
	private final InetSocketAddress address;
	private NioEventLoopGroup bossGroup = null;
	private NioEventLoopGroup workerGroup = null;
	private ServerBootstrap serverBootstrap = null;
	
	public void initialize() {
		final NetworkManager networkManager = new ServerNetworkManager(this);
		networkManager.registerDefaultCodecs();
		Transport.getInstance().setNetworkManager(networkManager);
		new Thread(new Runnable() {
			
			@Override
			public void run() {
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
			}
		}).start();
		bossGroup = new NioEventLoopGroup();
		workerGroup = new NioEventLoopGroup();
		serverBootstrap = new ServerBootstrap();
		serverBootstrap.group(bossGroup, workerGroup);
		serverBootstrap.channel(NioServerSocketChannel.class);
		serverBootstrap.option(ChannelOption.TCP_NODELAY, true).option(ChannelOption.SO_KEEPALIVE, true);
		serverBootstrap.childOption(ChannelOption.TCP_NODELAY, true).option(ChannelOption.SO_KEEPALIVE, true);
		final ChannelInitializer channelInitializer = new ChannelInitializer(NetworkDevice.SERVER);
		serverBootstrap.handler(channelInitializer);
		serverBootstrap.childHandler(channelInitializer);
		networkManager.registerEvent(new IMessageEvent<IMessage>() {
			
			@Override
			public void onReceived(IMessage message) {
				if (message instanceof HandleSubscriptionsMessage) {
					return;
				}
				if (message.getChannels().size() == 0) {
					broadcastMessage(message);
					return;
				} 
				sendMessage(message, message.getChannels().toArray(new String[message.getChannels().size()]));
			}
		});
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

	public boolean start() {
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			
			@Override
			public void run() {
				shutdown();
			}
		}));
		return serverBootstrap.bind(address).syncUninterruptibly().addListener(new GenericFutureListener<Future<? super Void>>() {
		
			public void operationComplete(Future<? super Void> future) throws Exception {
				if (future.cause() != null) {
					future.cause().printStackTrace();
				}
				onStarted();
			};
		}).syncUninterruptibly().isSuccess();
	}
	
	public void onStarted() {
		System.out.println("Successfully binded NetworkServer to " + address.toString());
	}
	
	public void broadcastMessage(IMessage message) {
		for (ISession session : Transport.getInstance().getNetworkManager().getSessions()) {
			session.send(message);
		}
	}
	
	public void sendMessage(IMessage message, String... channels) {
		final NetworkManager networkManager = Transport.getInstance().getNetworkManager();
		final Map<Channel, Set<String>> subscriptions = subcriptionManager.getSubscriptions();
		for (String channel : channels) {
			for (Entry<Channel, Set<String>> entry : subscriptions.entrySet()) {
				if (entry.getValue().contains(channel)) {
					networkManager.getSession(entry.getKey()).send(message);
				}
			}
		}
	}

	public void shutdown() {
		workerGroup.shutdownGracefully();
		bossGroup.shutdownGracefully();
		onShutdown();
	}
	
	public void onShutdown() {
		System.out.println("Successfully shutdown NetworkServer on " + address.toString());
	}

	public void destroy() {
		bossGroup = null;
		workerGroup = null;
		serverBootstrap = null;
	}
}
