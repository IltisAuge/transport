package de.iltisauge.transport.network;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import de.iltisauge.transport.packets.HandleSubscriptionsMessage;
import de.iltisauge.transport.packets.HandleSubscriptionsMessage.HandleSubscriptionType;
import de.iltisauge.transport.packets.TextMessage;
import de.iltisauge.transport.utils.Util;
import io.netty.channel.Channel;

public class NetworkManager {
	
	private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	private final Map<Class<?>, IMessageCodec<?>> codecs = new HashMap<Class<?>, IMessageCodec<?>>();
	private final Map<Class<?>, List<IMessageEvent<?>>> clazzBoundEvents = new HashMap<Class<?>, List<IMessageEvent<?>>>();
	private final Set<IMessageEvent<?>> nonboundEvents = new HashSet<IMessageEvent<?>>();
	private final Map<Channel, ISession> sessions = new HashMap<Channel, ISession>();
	private final Set<String> subscriptions = new HashSet<String>();
	
	public void registerDefaultCodecs() {
		registerCodec(TextMessage.class, TextMessage.CODEC);
		registerCodec(HandleSubscriptionsMessage.class, HandleSubscriptionsMessage.CODEC);
	}
	
	public void registerCodec(Class<?> clazz, IMessageCodec<?> codec) {
		lock.writeLock().lock();
		try {
			codecs.put(clazz, codec);
		} finally {
			lock.writeLock().unlock();
		}
	}
	
	public void unregisterCodec(Class<?> clazz) {
		lock.writeLock().lock();
		try {
			codecs.remove(clazz);
		} finally {
			lock.writeLock().unlock();
		}
	}
	
	public IMessageCodec<?> getCodec(Class<?> clazz) {
		lock.readLock().lock();
		try {
			final IMessageCodec<?> codec = codecs.get(clazz);
			if (codec == null) {
				System.out.println("No packet codec is registered for class " + clazz.getName());
				return null;
			}
			return codec;
		} finally {
			lock.readLock().unlock();
		}
	}
	
	public void registerEvent(IMessageEvent<?> event) {
		lock.writeLock().lock();
		try {
			nonboundEvents.add(event);
		} finally {
			lock.writeLock().unlock();
		}
	}
	
	public void unregisterEvent(IMessageEvent<?> event) {
		lock.writeLock().lock();
		try {
			nonboundEvents.remove(event);
		} finally {
			lock.writeLock().unlock();
		}
	}
	
	public void registerEvent(Class<?> clazz, IMessageEvent<?> event) {
		lock.writeLock().lock();
		try {
			if (clazzBoundEvents.containsKey(clazz)) {
				clazzBoundEvents.get(clazz).add(event);
			} else {
				clazzBoundEvents.put(clazz, new ArrayList<IMessageEvent<?>>(Arrays.asList(event)));
			}
		} finally {
			lock.writeLock().unlock();
		}
	}
	
	public void unregisterEvent(Class<?> clazz, IMessageEvent<?> event) {
		lock.writeLock().lock();
		try {
			clazzBoundEvents.remove(clazz, event);
		} finally {
			lock.writeLock().unlock();
		}
	}

	public void unregisterEvents(Class<?> clazz) {
		lock.writeLock().lock();
		try {
			clazzBoundEvents.remove(clazz);
		} finally {
			lock.writeLock().unlock();
		}
	}
	
	public List<IMessageEvent<?>> getEvents(Class<?> clazz) {
		lock.readLock().lock();
		try {
			final List<IMessageEvent<?>> out = new ArrayList<IMessageEvent<?>>();
			final List<IMessageEvent<?>> events = this.clazzBoundEvents.get(clazz);
			if (events != null) {
				out.addAll(events);
			}
			return out;
		} finally {
			lock.readLock().unlock();
		}
	}

	public List<IMessageEvent<?>> getEvents() {
		lock.readLock().lock();
		try {
			return new ArrayList<IMessageEvent<?>>(nonboundEvents);
		} finally {
			lock.readLock().unlock();
		}
	}
	
	public void registerSession(ISession session) {
		lock.writeLock().lock();
		try {
			sessions.put(session.getChannel(), session);
		} finally {
			lock.writeLock().unlock();
		}
	}

	public void unregisterSession(ISession session) {
		lock.writeLock().lock();
		try {
			sessions.remove(session.getChannel());
		} finally {
			lock.writeLock().unlock();
		}
	}

	public List<ISession> getSessions() {
		lock.readLock().lock();
		try {
			return new ArrayList<ISession>(sessions.values());
		} finally {
			lock.readLock().unlock();
		}
	}
	
	public ISession getSession(Channel channel) {
		lock.readLock().lock();
		try {
			final ISession session = sessions.get(channel);
			if (session == null) {
				System.out.println("No session is registered for channel " + channel.toString());
				return null;
			}
			return session;
		} finally {
			lock.readLock().unlock();
		}
	}
	
	public boolean isSubscribed(String channel) {
		return subscriptions.contains(channel);
	}
	
	
	public List<String> getSubscriptions() {
		return new ArrayList<String>(subscriptions);
	}
	
	public void addSubscriptions(String... channels) {
		System.out.println("Adding subs to " + Util.arrayToLine(channels));
		subscriptions.addAll(Arrays.asList(channels));
		final HandleSubscriptionsMessage packet = new HandleSubscriptionsMessage(HandleSubscriptionType.ADD, channels);
		packet.send("handle-subscriptions");
	}
	
	public void removeSubscriptions(String... channels) {
		subscriptions.removeAll(Arrays.asList(channels));
		final HandleSubscriptionsMessage packet = new HandleSubscriptionsMessage(HandleSubscriptionType.REMOVE, channels);
		packet.send("handle-subscriptions");
	}

	public void onSessionInactive(ISession session) { }
}
