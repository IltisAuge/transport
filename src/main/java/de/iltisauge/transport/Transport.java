package de.iltisauge.transport;

import de.iltisauge.transport.client.NetworkClient;
import de.iltisauge.transport.network.NetworkManager;
import lombok.Getter;
import lombok.Setter;

public class Transport {

	private static Transport INSTANCE;
	
	public static Transport getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new Transport();
		}
		return INSTANCE;
	}

	@Getter
	private NetworkManager networkManager = new NetworkManager();
	
	public void setNetworkManager(NetworkManager networkManager) {
		this.networkManager = networkManager;
	}
	/**
	 * Can be null if this instance runs a server.
	 */
	@Setter
	@Getter
	private NetworkClient networkClient;
}
