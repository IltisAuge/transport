package de.iltisauge.transport.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import de.iltisauge.transport.network.ISession;
import de.iltisauge.transport.utils.Util;
import io.netty.channel.Channel;
import lombok.Getter;

public class SubcriptionManager {
	
	private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	@Getter
	private final Map<Channel, Set<String>> subscriptions = new HashMap<Channel, Set<String>>();
	
	public void addSubscriptions(ISession session, String... channels) {
		final Channel channel = session.getChannel();
		lock.writeLock().lock();
		try {
			if (subscriptions.containsKey(channel)) {
				subscriptions.get(channel).addAll(Arrays.asList(channels));
			} else {
				subscriptions.put(channel, new HashSet<String>(Arrays.asList(channels)));
			}
			System.out.println("Added subscriptions (" + Util.arrayToLine(channels) + ") for channel " + channel);
		} finally {
			lock.writeLock().unlock();
		}
	}
	
	public void removeSubscriptions(ISession session, String... channels) {
		final Channel channel = session.getChannel();
		lock.writeLock().lock();
		try {
			if (subscriptions.containsKey(channel)) {
				subscriptions.get(channel).removeAll(Arrays.asList(channels));
			}
			System.out.println("Removed subscriptions (" + Util.arrayToLine(channels) + ") for channel " + channel);
		} finally {
			lock.writeLock().unlock();
		}
	}
	
	public void removeSubscriptions(ISession session) {
		final Channel channel = session.getChannel();
		lock.writeLock().lock();
		try {
			subscriptions.remove(channel);
			System.out.println("Removed all subscriptions for session " + session);
		} finally {
			lock.writeLock().unlock();
		}
	}
	
	public List<String> getSubscriptions(ISession session) {
		final Channel channel = session.getChannel();
		lock.writeLock().lock();
		try {
			final ArrayList<String> out = new ArrayList<String>();
			final Set<String> subscriptions = this.subscriptions.get(channel);
			if (subscriptions != null) {
				out.addAll(subscriptions);
			}
			return out;
		} finally {
			lock.writeLock().unlock();
		}
	}
}
