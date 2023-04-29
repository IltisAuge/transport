package de.iltisauge.transport.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import de.iltisauge.transport.Transport;
import de.iltisauge.transport.network.ISession;
import de.iltisauge.transport.network.NetworkManager;
import io.netty.channel.Channel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * This class extends {@link NetworkManager} and handles session management.<br>
 * The session cache is accessed in a thread-safe manner.
 * 
 * @author Daniel Ziegler
 *
 */
@RequiredArgsConstructor
public class ServerNetworkManager extends NetworkManager {
	
	@Getter
	private final SubscriptionManager subscriptionManager;
	private final Map<Channel, ISession> sessions = new HashMap<>();
	
	public void registerSession(ISession session) {
		synchronized (sessions) {
			sessions.put(session.getChannel(), session);
		}
		Transport.getLogger().log(Level.INFO, "Registered session " + session);
	}

	public void unregisterSession(ISession session) {
		synchronized (sessions) {
			sessions.remove(session.getChannel());
		}
		Transport.getLogger().log(Level.INFO, "Unregistered session " + session);
	}

	public List<ISession> getSessions() {
		synchronized (sessions) {
			return new ArrayList<>(sessions.values());
		}
	}
	
	public ISession getSession(Channel channel) {
		ISession session = null;
		synchronized (sessions) {
			session = sessions.get(channel);
		}
		if (session == null) {
			Transport.getLogger().log(Level.WARNING, "No session is registered for channel " + channel.toString());
			return null;
		}
		return session;
	}
	
	@Override
	public void onSessionInactive(ISession session) {
		subscriptionManager.removeSubscriptions(session);
	}
}
