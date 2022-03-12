package de.iltisauge.transport.network;

import io.netty.channel.Channel;
import lombok.RequiredArgsConstructor;

/**
 * This class uses a {@link io.netty.channel.ChannelInitializer} to add a {@link MessageEncoder}, {@link MessageDecoder} and the {@link ChannelInboundHandler} to a new channel's pipeline.
 * 
 * @author Daniel Ziegler
 *
 */
@RequiredArgsConstructor
public class ChannelInitializer extends io.netty.channel.ChannelInitializer<Channel> {

	private final NetworkDevice networkDevice;
	
	@Override
	protected void initChannel(Channel ch) throws Exception {
		ch.pipeline().addLast(new MessageEncoder(), new MessageDecoder(networkDevice), new ChannelInboundHandler(networkDevice));
	}
}