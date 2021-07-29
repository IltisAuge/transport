package de.iltisauge.transport.network;

import io.netty.channel.Channel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ChannelInitializer extends io.netty.channel.ChannelInitializer<Channel> {

	private final NetworkDevice networkDevice;
	
	@Override
	protected void initChannel(Channel ch) throws Exception {
		ch.pipeline().addLast(new MessageEncoder(), new MessageDecoder(), new ChannelInboundHandler(networkDevice));
	}
}