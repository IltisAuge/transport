package de.iltisauge.transport.network;

import de.iltisauge.transport.client.NetworkClient;
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
	private final NetworkManager networkManager = new NetworkManager();
	/**
	 * Can be null if this instance runs a server.
	 */
	@Setter
	@Getter
	private NetworkClient networkClient;
}
