package de.iltisauge.transport.network;

import java.net.SocketAddress;

import io.netty.channel.Channel;

/**
 * This interface can be used to create session classes.
 * 
 * @author Daniel Ziegler
 *
 */
public interface ISession {
	
	/**
	 * 
	 * @return the {@link Channel} object of the session.
	 */
	Channel getChannel();
	
	/**
	 * 
	 * @return the {@link SocketAddress} of the client-side.
	 */
	SocketAddress getClientAddress();
	
	/**
	 * 
	 * @return the {@link SocketAddress} of the server-side.
	 */
	SocketAddress getServerAddress();
	
	/**
	 * Sends the {@link Sendable} through the sessions {@link Channel}.<br>
	 * Set <code>logTraffic</code> to true if you want to log the sending of the message.
	 * 
	 * @param message
	 * @param logTraffic
	 * @return
	 */
	boolean send(Sendable message, boolean logTraffic);
	
	/**
	 * @see #send(Sendable, boolean)
	 */
	default boolean send(Sendable message) {
		return send(message, false);
	}
}
