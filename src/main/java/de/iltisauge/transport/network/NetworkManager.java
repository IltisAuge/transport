package de.iltisauge.transport.network;

import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Level;

import de.iltisauge.transport.Transport;
import de.iltisauge.transport.messages.HandleSubscriptionsMessage;
import de.iltisauge.transport.utils.CastUtil;

/**
 * This class handles many caches and network stuff.
 * All caches in this class are accessed in a thread-safe manner.
 * 
 * @author Daniel Ziegler
 *
 */
public class NetworkManager {
	
	private final Map<Class<?>, IMessageCodec<?>> codecs = new HashMap<>();
	private final Map<Class<?>, Map<IMessageEvent<?>, Integer>> clazzBoundEvents = new HashMap<>();
	private final Map<IMessageEvent<?>, Integer> notBoundEvents = new HashMap<>();
	private Integer latestEventPriority = 0;
	
	/**
	 * Registers the default message codecs.
	 */
	public void registerDefaultCodecs() {
		registerCodec(HandleSubscriptionsMessage.class, HandleSubscriptionsMessage.CODEC);
	}
	
	/**
	 * Unregisters the default message codecs.
	 */
	public void unregisterDefaultCodecs() {
		unregisterCodec(HandleSubscriptionsMessage.class);
	}
	
	/**
	 * Registers a {@link IMessageCodec} with a {@link Class} as the identifier.<br>
	 * @param clazz
	 * @param codec
	 */
	public void registerCodec(Class<?> clazz, IMessageCodec<?> codec) {
		synchronized (codecs) {
			codecs.put(clazz, codec);
		}
	}
	
	/**
	 * Unregisters a {@link IMessageCodec} by the {@link Class} identifier.<br>
	 * @param clazz
	 */
	public void unregisterCodec(Class<?> clazz) {
		synchronized (codecs) {
			codecs.remove(clazz);
		}
	}
	
	/**
	 * Checks if the codec cache contains the given class in its keyset.
	 * @param clazz
	 * @return true, if the codec is registered, otherwise false.
	 */
	public boolean isCodecRegistered(Class<?> clazz) {
		synchronized (codecs) {
			return codecs.containsKey(clazz);
		}
	}
	
	/**
	 * 
	 * @param clazz
	 * @return the {@link IMessageCodec} that is registered for the given class or null.
	 */
	public IMessageCodec<?> getCodec(Class<?> clazz) {
		IMessageCodec<?> codec = null;
		synchronized (codecs) {
			codec = codecs.get(clazz);
		}
		if (codec == null) {
			Transport.getLogger().log(Level.WARNING, "No packet codec is registered for class " + clazz.getName());
			return null;
		}
		return codec;
	}
	
	/**
	 * Registers a new {@link IMessageEvent} that is not bound to a specific class.<br>
	 * That causes the event to be fired for every sent and received message.
	 * @param event
	 */
	public void registerEvent(IMessageEvent<?> event) {
		Integer priority = null;
		synchronized (latestEventPriority) {
			priority = latestEventPriority;
			latestEventPriority++;
		}
		synchronized (notBoundEvents) {
			notBoundEvents.put(event, priority);
		}
	}
	
	/**
	 * Unregisters a {@link IMessageEvent} that is not bound to a specific class.
	 * @param event
	 */
	public void unregisterEvent(IMessageEvent<?> event) {
		synchronized (notBoundEvents) {
			notBoundEvents.remove(event);
		}
	}
	
	/**
	 * Registers a new {@link IMessageEvent} for the given class.<br>
	 * The event will be fired on every sent and received message that is an instance of the given class.
	 * @param clazz
	 * @param event
	 */
	public void registerEvent(Class<?> clazz, IMessageEvent<?> event) {
		Integer priority = null;
		synchronized (latestEventPriority) {
			priority = latestEventPriority;
			latestEventPriority++;
		}
		synchronized (clazzBoundEvents) {
			if (clazzBoundEvents.containsKey(clazz)) {
				final Map<IMessageEvent<?>, Integer> map = clazzBoundEvents.get(clazz);
				map.put(event, priority);
			} else {
				final Map<IMessageEvent<?>, Integer> map = new HashMap<>();
				map.put(event, priority);
				clazzBoundEvents.put(clazz, map);
			}
		}
	}
	
	/**
	 * Unregisters a {@link IMessageEvent} that is bound to a specific class.
	 * @param clazz
	 * @param event
	 */
	public void unregisterEvent(Class<?> clazz, IMessageEvent<?> event) {
		synchronized (clazzBoundEvents) {
			clazzBoundEvents.remove(clazz, event);
		}
	}

	/**
	 * Unregisters all {@link IMessageEvent}s for the given class.
	 * @param clazz
	 */
	public void unregisterEvents(Class<?> clazz) {
		synchronized (clazzBoundEvents) {
			clazzBoundEvents.remove(clazz);
		}
	}
	
	/**
	 * 
	 * @param clazz
	 * @return a {@link ArrayList} containing all {@link IMessageEvent}s that are registered for the given class.<br>
	 * If no events are registered for that class an empty list will be returned.
	 */
	public Map<IMessageEvent<?>, Integer> getEvents(Class<?> clazz) {
		Map<IMessageEvent<?>, Integer> events = null;
		synchronized (clazzBoundEvents) {
			events = clazzBoundEvents.get(clazz);
		}
		if (events != null) {
			return events;
		}
		return new HashMap<>();
	}

	/**
	 * 
	 * @return a {@link ArrayList} containing all {@link IMessageEvent}s that are not bound to a specific class.<br>
	 * If no non-bound events are registered an empty list will be returned.
	 */
	public Map<IMessageEvent<?>, Integer> getEvents() {
		synchronized (notBoundEvents) {
			return notBoundEvents;
		}
	}
	
	/**
	 * This method fires all outbound {@link IMessageEvent}s that are registered for the object's {@link Class}
	 * and all non-bound events.
	 * @param object
	 */
	public void fireOutboundMessageEvents(Sendable object) {
		fireMessageEvents(object, false);
	}

	/**
	 * This method fires all inbound {@link IMessageEvent}s that are registered for the object's {@link Class}
	 * and all non-bound events.
	 * @param object
	 */
	public void fireInboundMessageEvents(Sendable object) {
		fireMessageEvents(object, true);
	}

	private void fireMessageEvents(Sendable object, boolean isReceive) {
		final Map<IMessageEvent<?>, Integer> classBoundEvents = getEvents(object.getClass());
		final Map<IMessageEvent<?>, Integer> notBoundEvents = getEvents();
		final List<Map.Entry<IMessageEvent<?>, Integer>> list = new ArrayList<>(classBoundEvents.entrySet());
		list.addAll(notBoundEvents.entrySet());
		Collections.sort(list, Map.Entry.comparingByValue());
		final IMessageEvent<?>[] allEvents = new IMessageEvent[classBoundEvents.size() + notBoundEvents.size()];
		int i = 0;
		for (Map.Entry<IMessageEvent<?>, Integer> entry : list) {
			allEvents[i] = entry.getKey();
			i++;
		}
		for (IMessageEvent<?> event : allEvents) {
			if (isReceive) {
				event.onReceived(CastUtil.cast(object));
			} else {
				event.onSent(CastUtil.cast(object));
			}
		}
	}

	/**
	 * This method is called when a {@link ISession} becomes inactive.
	 * @param session
	 */
	public void onSessionInactive(ISession session) { }
}
