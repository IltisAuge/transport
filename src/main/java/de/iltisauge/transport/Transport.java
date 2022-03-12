package de.iltisauge.transport;

import java.util.logging.Logger;

import de.iltisauge.transport.client.NetworkClient;
import de.iltisauge.transport.network.NetworkManager;
import de.iltisauge.transport.server.NetworkServer;

/**
 * 
 * @author Daniel Ziegler
 *
 */
public final class Transport {
	
	private static NetworkClient CLIENT;
	private static NetworkServer SERVER;
	private static Logger LOGGER;

	/**
	 * Can be null if this instance runs a server.
	 */
	public static NetworkClient getClient() {
		return CLIENT;
	}
	
	/**
	 * Sets the {@link NetworkClient} for this transport instance.
	 * @param client
	 */
	public static void setClient(NetworkClient client) {
		CLIENT = client;
	}

	/**
	 * Can be null if this instance runs a client.
	 */
	public static NetworkServer getServer() {
		return SERVER;
	}
	
	/**
	 * Sets the {@link NetworkServer} for this transport instance.
	 * @param server
	 */
	public static void setServer(NetworkServer server) {
		SERVER = server;
	}
	
	/**
	 * 
	 * @return the {@link Logger} for this transport instance.
	 */
	public static Logger getLogger() {
		return LOGGER;
	}
	
	/**
	 * Sets the {@link Logger} for this transport instance.
	 * @param logger
	 */
	public static void setLogger(Logger logger) {
		LOGGER = logger;
	}
	
	/**
	 * 
	 * @return the {@link NetworkManager} of the {@link NetworkClient} or the {@link NetworkServer}. If both are null, null is returned.
	 */
	public static NetworkManager getNetworkManager() {
		return CLIENT != null ? CLIENT.getNetworkManager() : SERVER != null ? SERVER.getNetworkManager() : null;
	}
}
