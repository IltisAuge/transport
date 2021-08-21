package de.iltisauge.transport.network;

import java.io.IOException;
import java.net.SocketException;
import java.util.List;

import de.iltisauge.transport.Transport;
import de.iltisauge.transport.client.NetworkClient;
import de.iltisauge.transport.utils.CastUtil;
import de.iltisauge.transport.utils.Util;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ChannelInboundHandler extends SimpleChannelInboundHandler<IMessage> {

	private final NetworkDevice networkDevice;
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		if (ctx.channel().remoteAddress() == null) {
			return;
		}
		final ISession session = Util.getSession(ctx, networkDevice);
		final Transport instance = Transport.getInstance();
		instance.getNetworkManager().registerSession(session);
		final NetworkClient networkClient = instance.getNetworkClient();
		if (networkClient != null) {
			networkClient.setSession(session);
		}
	}
	
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		final ISession session = Util.getSession(ctx, networkDevice);
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
