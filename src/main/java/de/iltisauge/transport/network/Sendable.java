package de.iltisauge.transport.network;

import de.iltisauge.transport.server.NetworkServer;

/**
 * This class is implemented by {@link IMessage} and {@link ServerMessageWrapper}.<br>
 * If you want to create your own message class implement {@link IMessage}.
 * 
 * @author Daniel Ziegler
 *
 */
public interface Sendable {

	/**
	 * 
	 * @return the {@link ISession} that the {@link Sendable} came from.
	 */
	ISession getFrom();
	
	/**
	 * Sets the {@link ISession} that the {@link Sendable} came from.
	 * @param session
	 */
	void setFrom(ISession session);
	
	/**
	 * 
	 * @return true, if the {@link NetworkServer} sends back this {@link Sendable} to the client who sent it, otherwise false.
	 */
	boolean isReceiveSelf();
	
	/**
	 * Set this to true, if you want to receive this {@link Sendable} too after sending it to the server.
	 * @param value
	 */
	void setReceiveSelf(boolean value);
	
	/**
	 * Adds channels to which the {@link Sendable} will be sent to.
	 * @param channels
	 */
	void addChannels(String... channels);
	
	/**
	 * Removes channels to which the {@link Sendable} will be sent to.
	 * @param channels
	 */
	void removeChannels(String... channels);
	
	/**
	 * 
	 * @return a {@link String[]} containing all channels the {@link Sendable} will be sent to.
	 */
	String[] getChannels();

	/**
	 * Sends this {@link Sendable} through the session
	 * @param channels
	 * @return true, if the action was successful, false otherwise
	 */
	boolean send(String... channels);

}
