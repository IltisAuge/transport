package de.iltisauge.transport.client;

import java.net.InetSocketAddress;
import java.util.Arrays;

import de.iltisauge.transport.network.ChannelInitializer;
import de.iltisauge.transport.network.IMessage;
import de.iltisauge.transport.network.ISession;
import de.iltisauge.transport.network.NetworkDevice;
import de.iltisauge.transport.network.NetworkManager;
import de.iltisauge.transport.network.Transport;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class NetworkClient {

	/*public static void main(String[] args) throws Exception {
		int port = -1;
		System.out.println("Gebe den Port des PacketServers an:");
		final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		while (port == -1) {
			final String line = reader.readLine();
			if (!Utils.isNumberic(line)) {
				System.out.println("Gebe eine gültige Zahl als Port an:");
				continue;
			}
			port = Integer.valueOf(line);
		}
		final INetworkClient networkClient = new NetworkClient(new InetSocketAddress("127.0.0.1", port));
		final PacketTransportAPI api = new PacketTransportAPI();
		api.setNetworkClient(networkClient);
		api.initialize();
		PacketTransport.setAPI(api);
		final INetworkManager networkManager = api.getNetworkManager();
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
						} else if (line.startsWith("say")) {
							final String text = line.substring(4);
							final TestPacket packet = new TestPacket(text);
							System.out.println("Sending TestPacket");
							packet.send("text-message");
						} else if (line.startsWith("sub")) {
							final String channel = line.substring(4);
							System.out.println("addSubscriptions " + channel);
							networkManager.addSubscriptions(channel);
						} else if (line.startsWith("unsub")) {
							final String channel = line.substring(6);
							System.out.println("removeSubscriptions " + channel);
							networkManager.removeSubscriptions(channel);
						}
					}
				} catch (IOException exception) {
					exception.printStackTrace();
				}
			}
		}).start();
		networkClient.initialize();
		networkManager.registerEvent(TestPacket.class, new IPacketEvent<TestPacket>() {
			
			@Override
			public void onReceived(TestPacket packet) {
				System.out.println("Message: " + packet.getText());
			}
		});
		networkClient.start();
	}*/
	
	public static void main(String[] args) {
		final NetworkClient client = new NetworkClient(new InetSocketAddress("127.0.0.1", 5001));
		client.initialize();
		client.start();
	}
	
	private final InetSocketAddress address;
	private NioEventLoopGroup eventLoopGroup = null;
	private Bootstrap bootstrap = null;
	private ISession session;
	
	public void initialize() {
		final NetworkManager networkManager = Transport.getInstance().getNetworkManager();
		networkManager.registerDefaultCodecs();
		eventLoopGroup = new NioEventLoopGroup();
		bootstrap = new Bootstrap();
		bootstrap.group(eventLoopGroup);
		bootstrap.channel(NioSocketChannel.class);
		bootstrap.option(ChannelOption.TCP_NODELAY, true).option(ChannelOption.SO_KEEPALIVE, true);
		final ChannelInitializer channelInitializer = new ChannelInitializer(NetworkDevice.CLIENT);
		bootstrap.handler(channelInitializer);
	}

	public boolean start() {
		System.out.println("Connecting to NetworkServer on " + address.toString() + "...");
		return bootstrap.connect(address).addListener(new GenericFutureListener<Future<? super Void>>() {
		
			public void operationComplete(Future<? super Void> future) throws Exception {
				if (future.cause() != null) {
					future.cause().printStackTrace();
					return;
				}
				onStarted();
			};
		}).isSuccess();
	}
	
	public void onStarted() {
		System.out.println("Successfully connected to NetworkServer on " + address.toString());
	}
	
	public void setSession(ISession session) {
		this.session = session;
		System.out.println("SESSION: " + session.toString());
	}

	public boolean send(IMessage message, String... channels) {
		message.getChannels().addAll(Arrays.asList(channels));
		return session.send(message);
	}
	
	public void shutdown() {
		eventLoopGroup.shutdownGracefully();
		onShutdown();
	}
	
	public void onShutdown() {
		System.out.println("Successfully disconnected from NetworkServer on " + address.toString());
	}

	public void destroy() {
		eventLoopGroup = null;
		bootstrap = null;
	}
}
