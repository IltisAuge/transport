package de.iltisauge.transport.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.iltisauge.transport.messages.HandleSubscriptionsMessage;
import de.iltisauge.transport.messages.HandleSubscriptionsMessage.HandleSubscriptionType;
import de.iltisauge.transport.network.NetworkManager;

/**
 * This class extends {@link NetworkManager} and contains channel subscription management.<br>
 * The subscription cache is accessed in a thread-safe manner.
 * 
 * @author Daniel Ziegler
 *
 */
public class ClientNetworkManager extends NetworkManager {

	private final Set<String> subscriptions = new HashSet<String>();
	
	/**
	 * 
	 * @param channel
	 * @return true if the {@link NetworkClient} is subscribed to the given channel, otherwise false. 
	 */
	public boolean isSubscribed(String channel) {
		synchronized (subscriptions) {
			return subscriptions.contains(channel);
		}
	}
	
	/**
	 * 
	 * @return a {@link ArrayList} containing all current channel subscriptions.
	 */
	public List<String> getSubscriptions() {
		synchronized (subscriptions) {
			return new ArrayList<String>(subscriptions);
		}
	}
	
	/**
	 * Sends a {@link HandleSubscriptionsMessage} message to the server to subscribe the given channels.<br>
	 * The message will be send through the <code>handle-subscriptions</code> channel.
	 * @param channels
	 */
	public void addSubscriptions(String... channels) {
		synchronized (subscriptions) {
			subscriptions.addAll(Arrays.asList(channels));
		}
		final HandleSubscriptionsMessage packet = new HandleSubscriptionsMessage(HandleSubscriptionType.ADD, channels);
		packet.send("handle-subscriptions");
	}
	
	/**
	 * Sends a {@link HandleSubscriptionsMessage} message to the server to subscribe the given channels.<br>
	 * The message will be send through the <code>handle-subscriptions</code> channel.
	 * @param channels
	 */
	public void removeSubscriptions(String... channels) {
		synchronized (subscriptions) {
			subscriptions.removeAll(Arrays.asList(channels));
		}
		final HandleSubscriptionsMessage packet = new HandleSubscriptionsMessage(HandleSubscriptionType.REMOVE, channels);
		packet.send("handle-subscriptions");
	}
}
