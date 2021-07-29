package de.iltisauge.transport.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import de.iltisauge.transport.network.ChannelInitializer;
import de.iltisauge.transport.network.IMessage;
import de.iltisauge.transport.network.ISession;
import de.iltisauge.transport.network.NetworkDevice;
import de.iltisauge.transport.network.NetworkManager;
import de.iltisauge.transport.network.Transport;
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
	
	/*public static void main(String[] args) throws Exception {
		int port = -1;
		System.out.println("Gebe den Startport an:");
		final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		while (port == -1) {
			final String line = reader.readLine();
			if (!Utils.isNumberic(line)) {
				System.out.println("Gebe eine gültige Zahl als Startport an:");
				continue;
			}
			port = Integer.valueOf(line);
		}
		final PacketTransportAPI api = new PacketTransportAPI();
		api.initialize();
		PacketTransport.setAPI(api);
		final SubcriptionManager subcriptionManager = new SubcriptionManager();
		final INetworkManager networkManager = api.getNetworkManager();
		networkManager.registerDefaultCodecs();
		final NetworkServer networkServer = new NetworkServer(subcriptionManager, new InetSocketAddress("127.0.0.1", port));
		networkServer.initialize();
		networkServer.start();
		networkManager.registerEvent(new IPacketEvent<IPacket>() {
			
			@Override
			public void onReceived(IPacket packet) {
				if (packet instanceof HandleSubscriptionsPacket) {
					return;
				}
				System.out.println("Packet from " + packet.getFrom());
				networkServer.sendPacket(packet, packet.getChannels().toArray(new String[packet.getChannels().size()]));
			}
		});
		networkManager.registerEvent(HandleSubscriptionsPacket.class, new IPacketEvent<HandleSubscriptionsPacket>() {
			
			@Override
			public void onReceived(HandleSubscriptionsPacket packet) {
				final HandleSubscriptionType handleSubscriptionType = packet.getHandleSubscriptionType();
				if (handleSubscriptionType.equals(HandleSubscriptionType.ADD)) {
					subcriptionManager.addSubscriptions(packet.getFrom(), packet.getChannelsToSubscribe());
				} else if (handleSubscriptionType.equals(HandleSubscriptionType.REMOVE)) {
					subcriptionManager.removeSubscriptions(packet.getFrom(), packet.getChannelsToSubscribe());
				}
			}
		});
	}*/
	
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
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
				String line = null;
				try {
					while ((line = reader.readLine()) != null) {
						System.out.println("line " + line);
						if (line.startsWith("stop")) {
							System.exit(0);
						}/* else if (line.startsWith("bc")) {
							System.out.println("Broadcasting..");
							final TestPacket packet = new TestPacket(line.substring(3));
							packet.addChannels("text-message");
							sendPacketToAllSessions(packet);
						}*/
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
	}

	public boolean start() {
		return serverBootstrap.bind(address).addListener(new GenericFutureListener<Future<? super Void>>() {
		
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
	
	public void sendPacketToAllSessions(IMessage packet) {
		for (ISession session : Transport.getInstance().getNetworkManager().getSessions()) {
			System.out.println("Send packet to session " + session);
			session.send(packet);
		}
	}
	
	public void sendPacket(IMessage packet, String... channels) {
		final NetworkManager networkManager = Transport.getInstance().getNetworkManager();
		final Map<Channel, Set<String>> subscriptions = subcriptionManager.getSubscriptions();
		for (String channel : packet.getChannels()) {
			for (Entry<Channel, Set<String>> entry : subscriptions.entrySet()) {
				if (entry.getValue().contains(channel)) {
					networkManager.getSession(entry.getKey()).send(packet);
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
