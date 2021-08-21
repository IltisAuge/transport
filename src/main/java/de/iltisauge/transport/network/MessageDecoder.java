package de.iltisauge.transport.network;

import java.util.List;

import de.iltisauge.transport.Transport;
import de.iltisauge.transport.utils.PacketUtil;
import de.iltisauge.transport.utils.Util;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.EmptyByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MessageDecoder extends ByteToMessageDecoder {

	private final NetworkDevice networkDevice;

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		if (in instanceof EmptyByteBuf) {
			return;
		}
		final String clazzName = PacketUtil.readString(in);
		final Class<?> clazz = Class.forName(clazzName);
		final int length = in.readInt();
		final String[] channels = new String[length];
		for (int i = 0; i < length; i++) {
			channels[i] = PacketUtil.readString(in);
		}
		final IMessageCodec<?> codec = Transport.getInstance().getNetworkManager().getCodec(clazz);
		if (codec != null) {
			final IMessage packet = (IMessage) codec.read(in);
			packet.setFrom(Util.getSession(ctx, networkDevice));
			packet.addChannels(channels);
			out.add(packet);
		}
	}
}
