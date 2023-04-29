package de.iltisauge.transport.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.iltisauge.transport.network.ISession;
import io.netty.channel.Channel;
import lombok.Getter;

/**
 * This class is used to handle channel subscriptions on the server-side.<br>
 * The subscription cache is accessed in a thread-safe manner.
 * 
 * @author Daniel Ziegler
 *
 */
public class SubscriptionManager {
	
	@Getter
	private final Map<ISession, Set<String>> subscriptions = new HashMap<>();
	
	/**
	 * Adds one or multiple channel subscription/s for the given session.
	 * @param session
	 * @param channels
	 */
	public void addSubscriptions(ISession session, String... channels) {
		synchronized (subscriptions) {
			if (subscriptions.containsKey(session)) {
				subscriptions.get(session).addAll(Arrays.asList(channels));
			} else {
				subscriptions.put(session, new HashSet<>(Arrays.asList(channels)));
			}
		}
	}
	
	/**
	 * Removes one or multiple channel subscription/s for the given session.
	 * @param session
	 * @param channels
	 */
	public void removeSubscriptions(ISession session, String... channels) {
		synchronized (subscriptions) {
			if (subscriptions.containsKey(session)) {
				subscriptions.get(session).removeAll(Arrays.asList(channels));
			}
		}
	}
	
	/**
	 * Removes all subscriptions for the given session.
	 * @param session
	 */
	public void removeSubscriptions(ISession session) {
		final Channel channel = session.getChannel();
		synchronized (subscriptions) {
			subscriptions.remove(channel);
		}
	}
	
	/**
	 * 
	 * @param session
	 * @return a {@link ArrayList} containing all current subscriptions for the given session.
	 */
	public List<String> getSubscriptions(ISession session) {
		final Channel channel = session.getChannel();
		final List<String> out = new ArrayList<>();
		synchronized (subscriptions) {
			final Set<String> subscriptions = this.subscriptions.get(channel);
			if (subscriptions != null) {
				out.addAll(subscriptions);
			}
			return out;
		}
	}
}
