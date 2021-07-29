package de.iltisauge.transport.server;

import de.iltisauge.transport.network.ISession;
import de.iltisauge.transport.network.NetworkManager;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ServerNetworkManager extends NetworkManager {
	
	@Getter
	private final NetworkServer networkServer;
	
	@Override
	public void onSessionInactive(ISession session) {
		networkServer.getSubcriptionManager().removeSubscriptions(session);
	}
}
