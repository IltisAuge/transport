package de.iltisauge.transport.network;

import de.iltisauge.transport.client.NetworkClient;
import de.iltisauge.transport.server.NetworkServer;
import lombok.Getter;
import lombok.Setter;

/**
 * This class is extended by {@link NetworkServer} and {@link NetworkClient}.
 * 
 * @author Daniel Ziegler
 *
 */
public abstract class NetworkDevice {
	
	@Getter
	@Setter
	private boolean logTraffic = true;
	
	/**
	 * Initializes the {@link NetworkDevice}.
	 */
	public abstract void initialize();
	
	/**
	 * Starts the {@link NetworkDevice}.<br>
	 * You can set <code>showTraffic</code> to true if you want
	 * to log all in-/outbound packets and connections.
	 * 
	 * @return true, if the the NetworkDevice has been started up successfully, otherwise false.
	 */
	public abstract boolean start(boolean showTraffic);
	
	/**
	 * @see #start(boolean)
	 */
	public boolean start() {
		return start(false);
	}
	

	/**
	 * This method is called when {@link #start(boolean)} has been executed.
	 */
	public abstract void onStarted();
	
	/**
	 * 
	 * @return whether the {@link NetworkDevice} is running or not.
	 */
	public abstract boolean isRunning();
	
	/**
	 * Shuts down the NetworkDevice.
	 */
	public abstract void shutdown();

	/**
	 * This method is called when the NetworkDevice has shut down.
	 */
	public abstract void onShutdown();
	
	/**
	 * Destroys the NetworkDevice.
	 */
	public abstract void destroy();

}
