package de.iltisauge.transport.network;

import de.iltisauge.transport.Transport;
import de.iltisauge.transport.utils.PacketUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class MessageEncoder extends MessageToByteEncoder<IMessage> {

	@Override
	protected void encode(ChannelHandlerContext ctx, IMessage msg, ByteBuf out) throws Exception {
		final Class<?> clazz = msg.getClass();
		PacketUtil.writeString(out, clazz.getName());
		out.writeInt(msg.getChannels().size());
		for (String channel : msg.getChannels()) {
			PacketUtil.writeString(out, channel);
		}
		@SuppressWarnings("unchecked")
		final IMessageCodec<IMessage> codec = (IMessageCodec<IMessage>) Transport.getInstance().getNetworkManager().getCodec(clazz);
		if (codec != null) {
			codec.write(out, msg);
		}
	}
}
