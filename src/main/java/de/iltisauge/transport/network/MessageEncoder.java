package de.iltisauge.transport.network;

import de.iltisauge.transport.Transport;
import de.iltisauge.transport.utils.PacketUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.RequiredArgsConstructor;

/**
 * This class extends {@link MessageToByteEncoder} and encodes a {@link Sendable} object to a {@link ByteBuf}.
 * If the {@link Sendable} object is a {@link ServerMessageWrapper} the unread class codec gets copied into the out-ByteBuf.
 * 
 * @author Daniel Ziegler
 *
 */
@RequiredArgsConstructor
public class MessageEncoder extends MessageToByteEncoder<Sendable> {
	
	@Override
	protected void encode(ChannelHandlerContext ctx, Sendable object, ByteBuf out) throws Exception {
		if (object instanceof ServerMessageWrapper) {
			final ServerMessageWrapper msg = (ServerMessageWrapper) object;
			PacketUtil.writeString(out, msg.getClassName());
			out.writeInt(msg.getChannels().length);
			for (String channel : msg.getChannels()) {
				PacketUtil.writeString(out, channel);
			}
			out.writeBoolean(msg.isReceiveSelf());
			out.writeBytes(msg.getBufferCopy()); // Write unread codec
		} else if (object instanceof IMessage) {
			final IMessage msg = (IMessage) object;
			final Class<?> clazz = msg.getClass();
			PacketUtil.writeString(out, clazz.getName());
			out.writeInt(msg.getChannels().length);
			for (String channel : msg.getChannels()) {
				PacketUtil.writeString(out, channel);
			}
			out.writeBoolean(msg.isReceiveSelf());
			@SuppressWarnings("unchecked")
			final IMessageCodec<IMessage> codec = (IMessageCodec<IMessage>) Transport.getNetworkManager().getCodec(clazz);
			if (codec != null) {
				codec.write(out, msg);
			}
 		}
	}
}
