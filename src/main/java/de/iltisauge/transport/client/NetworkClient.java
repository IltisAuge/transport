package de.iltisauge.transport.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.util.Arrays;

import de.iltisauge.transport.Transport;
import de.iltisauge.transport.messages.TextMessage;
import de.iltisauge.transport.network.ChannelInitializer;
import de.iltisauge.transport.network.IMessage;
import de.iltisauge.transport.network.IMessageEvent;
import de.iltisauge.transport.network.ISession;
import de.iltisauge.transport.network.NetworkDevice;
import de.iltisauge.transport.network.NetworkManager;
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
	
	public static void main(String[] args) {
		final NetworkClient client = new NetworkClient(new InetSocketAddress("127.0.0.1", 5001));
		Transport.getInstance().setNetworkClient(client);
		client.initialize();
		if (!client.start()) {
			System.exit(0);
			return;
		}
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
							final TextMessage textMessage = new TextMessage(line);
							textMessage.send();
						}
					}
				} catch (IOException exception) {
					exception.printStackTrace();
				}
			}
		}).start();
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
		networkManager.registerEvent(TextMessage.class, new IMessageEvent<TextMessage>() {
			
			@Override
			public void onReceived(TextMessage message) {
				System.out.println("Message: " + message.getText());
			}
		});
	}

	public boolean start() {
		System.out.println("Connecting to NetworkServer on " + address.toString() + "...");
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
					onStarted();
				};
			}).isSuccess();
		} catch (Exception exception) {
			if (exception instanceof ConnectException && exception.getMessage().contains("Connection refused: no further information")) {
				System.out.println("The NetworkServer is unreachable! Please try again later!");
				return false;
			}
		}
		return false;
	}
	
	public void onStarted() {
		System.out.println("Successfully connected to NetworkServer on " + address.toString());
	}
	
	public boolean isConnected() {
		return session != null;
	}
	
	public void setSession(ISession session) {
		this.session = session;
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
