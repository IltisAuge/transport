package de.iltisauge.transport.network;

import java.io.IOException;
import java.net.SocketException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.stream.Collectors;

import de.iltisauge.transport.Transport;
import de.iltisauge.transport.client.NetworkClient;
import de.iltisauge.transport.server.NetworkServer;
import de.iltisauge.transport.utils.Util;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.RequiredArgsConstructor;

/**
 * This class extends {@link SimpleChannelInboundHandler} and handles all channel activities such as {@link #channelActive(ChannelHandlerContext)},
 * {@link #channelInactive(ChannelHandlerContext)}, {@link #exceptionCaught(ChannelHandlerContext, Throwable)} or {@link #channelRead0(ChannelHandlerContext, Sendable)}.
 * 
 * @author Daniel Ziegler
 *
 */
@RequiredArgsConstructor
public class ChannelInboundHandler extends SimpleChannelInboundHandler<Sendable> {

	private final NetworkDevice networkDevice;
	
	/**
	 * This method is called when a connection between a {@link NetworkClient} and the {@link NetworkServer} has been established.
	 */
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		if (ctx.channel().remoteAddress() == null) {
			return;
		}
		final ISession session = Util.getSession(ctx, networkDevice);
		if (Transport.getServer() != null) {
			Transport.getServer().getNetworkManager().registerSession(session);
		}
		final NetworkClient networkClient = Transport.getClient();
		if (networkClient != null) {
			networkClient.setSession(session);
		}
		if (networkDevice.isLogTraffic()) {
			Transport.getLogger().log(Level.INFO, "[<->] Connection established with " + ctx.channel().remoteAddress());
		}
	}
	
	/**
	 * This method is called when connection between a {@link NetworkClient} and the {@link NetworkServer} has been interrupted. 
	 */
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		final ISession session = Util.getSession(ctx, networkDevice);
		if (Transport.getServer() != null) {
			Transport.getServer().getNetworkManager().unregisterSession(session);
		}
		final NetworkClient networkClient = Transport.getClient();
		if (networkClient != null) {
			networkClient.setSession(null);
		}
		Transport.getNetworkManager().onSessionInactive(session);
		if (networkDevice.isLogTraffic()) {
			Transport.getLogger().log(Level.INFO, "[<-/->] Connection interrupted with " + ctx.channel().remoteAddress());
		}
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
	
	/**
	 * This method is called when a {@link Sendable} was decoded.<br>
	 * If the {@link #networkDevice} is not a {@link NetworkServer} and the object is not a {@link ServerMessageWrapper} the registered {@link IMessageEvent}s get called.
	 */
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Sendable object) throws Exception {
		if (networkDevice.isLogTraffic()) {
			Transport.getLogger().log(Level.INFO, "[<-] " + object.getClass().getName() + " (" + Arrays.asList(object.getChannels()).stream().collect(Collectors.joining(", ")) + ")");
		}
		if (networkDevice instanceof NetworkServer && object instanceof ServerMessageWrapper) {
			((NetworkServer) networkDevice).forwardMessage((ServerMessageWrapper) object);
			return;
		}
		if (object instanceof IMessage) {
			Transport.getNetworkManager().fireMessageEvents((IMessage) object);
		}
	}
}
