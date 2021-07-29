package de.iltisauge.transport.network;

import java.net.SocketAddress;

import io.netty.channel.Channel;

public interface ISession {
	
	Channel getChannel();
	
	SocketAddress getClientAddress();
	
	SocketAddress getServerAddress();
	
	boolean send(IMessage packet);

}
