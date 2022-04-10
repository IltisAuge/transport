package de.iltisauge.transport.network;

import java.util.List;

import de.iltisauge.transport.Transport;
import de.iltisauge.transport.server.NetworkServer;
import de.iltisauge.transport.utils.PacketUtil;
import de.iltisauge.transport.utils.Util;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.EmptyByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.RequiredArgsConstructor;

/**
 * This class extends {@link ByteToMessageDecoder}.<br>
 * The {@link ByteBuf} will be decoded by reading the class name of the {@link Sendable} first,
 * followed by the channels to which the message should be send to.<br>
 * If the network device that decodes the message is a {@link NetworkServer} and the class with the read class name can not be found or no codec is registered for that class,
 * a {@link ServerMessageWrapper} will be created, with the unread class codec left in the ByteBuf.
 * 
 * @author Daniel Ziegler
 *
 */
@RequiredArgsConstructor
public class MessageDecoder extends ByteToMessageDecoder {

	private final NetworkDevice networkDevice;

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		if (in instanceof EmptyByteBuf) {
			return;
		}
		final ISession from = Util.getSession(ctx, networkDevice);
		final String clazzName = PacketUtil.readString(in);
		Class<?> clazz = null;
		try {
			clazz = Class.forName(clazzName);
		} catch (ClassNotFoundException exception) {
		}
		final int length = in.readInt();
		final String[] channels = new String[length];
		for (int i = 0; i < length; i++) {
			channels[i] = PacketUtil.readString(in);
		}
		final boolean isReceiveSelf = in.readBoolean();
		Sendable sendable = null;
		if ((clazz == null || !Transport.getNetworkManager().isCodecRegistered(clazz)) && networkDevice instanceof NetworkServer) {
			final ByteBuf codecBuffer = in.readBytes(in.readableBytes());
			sendable = new ServerMessageWrapper(clazzName, codecBuffer);
		} else {
			final IMessageCodec<?> codec = Transport.getNetworkManager().getCodec(clazz);
			sendable = (IMessage) codec.read(in);
		}
		sendable.setFrom(from);
		sendable.addChannels(channels);
		sendable.setReceiveSelf(isReceiveSelf);
		out.add(sendable);
	}
}
