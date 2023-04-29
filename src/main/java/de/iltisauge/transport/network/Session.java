package de.iltisauge.transport.network;

import java.net.SocketAddress;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.stream.Collectors;

import de.iltisauge.transport.Transport;
import io.netty.channel.Channel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * This class implements {@link ISession} and is used to send {@link IMessage} through the network.
 * 
 * @author Daniel Ziegler
 *
 */
@RequiredArgsConstructor
@Getter
public class Session implements ISession {

	private final Channel channel;
	private final SocketAddress clientAddress;
	private final SocketAddress serverAddress;
	
	@Override
	public boolean send(Sendable message, boolean logTraffic) {
		final boolean success = channel.writeAndFlush(message).syncUninterruptibly().isSuccess();
		Transport.getNetworkManager().fireOutboundMessageEvents(message);
		return success;
	}
}
