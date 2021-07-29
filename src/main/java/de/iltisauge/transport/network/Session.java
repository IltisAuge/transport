package de.iltisauge.transport.network;

import java.net.SocketAddress;

import io.netty.channel.Channel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class Session implements ISession {

	private final Channel channel;
	private final SocketAddress clientAddress;
	private final SocketAddress serverAddress;

	@Override
	public boolean send(IMessage message) {
		return channel.writeAndFlush(message).syncUninterruptibly().isSuccess();
	}
	
	@Override
	public String toString() {
		return channel.toString();
	}
}
