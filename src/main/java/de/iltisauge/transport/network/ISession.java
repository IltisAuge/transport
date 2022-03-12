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
	 * Sends the {@link IMessage} through the sessions {@link Channel}.<br>
	 * Set <code>logTraffic</code> to true if you want to log the sending of the message.
	 * 
	 * @param message
	 * @param logTraffic
	 * @return
	 */
	boolean send(IMessage message, boolean logTraffic);
	
	/**
	 * @see #send(IMessage, boolean)
	 */
	default boolean send(IMessage message) {
		return send(message, false);
	}
}
