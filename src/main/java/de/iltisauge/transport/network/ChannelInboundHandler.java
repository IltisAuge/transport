package de.iltisauge.transport.network;

import java.io.IOException;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.List;

import de.iltisauge.transport.Transport;
import de.iltisauge.transport.client.NetworkClient;
import de.iltisauge.transport.utils.CastUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class ChannelInboundHandler extends SimpleChannelInboundHandler<IMessage> {

	private static NetworkDevice NETWORK_DEVICE;
	
	public ChannelInboundHandler(NetworkDevice networkDevice) {
		NETWORK_DEVICE = networkDevice;
	}
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		if (ctx.channel().remoteAddress() == null) {
			return;
		}
		final ISession session = getSession(ctx);
		final Transport instance = Transport.getInstance();
		instance.getNetworkManager().registerSession(session);
		final NetworkClient networkClient = instance.getNetworkClient();
		if (networkClient != null) {
			networkClient.setSession(session);
		}
	}
	
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		final ISession session = getSession(ctx);
		final Transport instance = Transport.getInstance();
		final NetworkManager networkManager = instance.getNetworkManager();
		networkManager.unregisterSession(session);
		final NetworkClient networkClient = instance.getNetworkClient();
		if (networkClient != null) {
			networkClient.setSession(null);
		}
		networkManager.onSessionInactive(session);
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		if ((cause instanceof IOException || cause instanceof SocketException)
				&& (cause.getMessage().equals("Connection reset")
					|| cause.getMessage().equals("Connection reset by peer")
					|| cause.getMessage().equals("Eine vorhandene Verbindung wurde vom Remotehost geschlossen"))) {
			return;
		}
		cause.printStackTrace();
	}

	public static Session getSession(ChannelHandlerContext ctx) {
		final Channel channel = ctx.channel();
		SocketAddress clientAddress = null;
		SocketAddress serverAddress = null;
		if (NETWORK_DEVICE.equals(NetworkDevice.SERVER)) {
			clientAddress = channel.remoteAddress();
			serverAddress = channel.localAddress();
		} else if (NETWORK_DEVICE.equals(NetworkDevice.CLIENT)) {
			clientAddress = channel.localAddress();
			serverAddress = channel.remoteAddress();
		}
		return new Session(channel, clientAddress, serverAddress);
	}
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, IMessage msg) throws Exception {
		final Class<?> clazz = msg.getClass();
		final List<IMessageEvent<?>> classBoundedEvents = Transport.getInstance().getNetworkManager().getEvents(clazz);
		if (classBoundedEvents != null) {
			for (IMessageEvent<?> event : classBoundedEvents) {
				event.onReceived(CastUtil.cast(msg));
			}
		}
		final List<IMessageEvent<?>> nonBoundedEvents = Transport.getInstance().getNetworkManager().getEvents();
		if (nonBoundedEvents != null) {
			for (IMessageEvent<?> event : nonBoundedEvents) {
				event.onReceived(CastUtil.cast(msg));
			}
		}
	}
}
