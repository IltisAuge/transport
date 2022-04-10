package de.iltisauge.transport.network;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
	
	private final Map<Class<?>, IMessageCodec<?>> codecs = new HashMap<Class<?>, IMessageCodec<?>>();
	private final Map<Class<?>, List<IMessageEvent<?>>> clazzBoundEvents = new HashMap<Class<?>, List<IMessageEvent<?>>>();
	private final Set<IMessageEvent<?>> nonboundEvents = new HashSet<IMessageEvent<?>>();
	
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
		synchronized (nonboundEvents) {
			nonboundEvents.add(event);
		}
	}
	
	/**
	 * Unregisters a {@link IMessageEvent} that is not bound to a specific class.
	 * @param event
	 */
	public void unregisterEvent(IMessageEvent<?> event) {
		synchronized (nonboundEvents) {
			nonboundEvents.remove(event);
		}
	}
	
	/**
	 * Registers a new {@link IMessageEvent} for the given class.<br>
	 * The event will be fired on every sent and received message that is an instance of the given class.
	 * @param clazz
	 * @param event
	 */
	public void registerEvent(Class<?> clazz, IMessageEvent<?> event) {
		synchronized (clazzBoundEvents) {
			if (clazzBoundEvents.containsKey(clazz)) {
				clazzBoundEvents.get(clazz).add(event);
			} else {
				clazzBoundEvents.put(clazz, new ArrayList<IMessageEvent<?>>(Arrays.asList(event)));
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
	public List<IMessageEvent<?>> getEvents(Class<?> clazz) {
		final List<IMessageEvent<?>> out = new ArrayList<IMessageEvent<?>>();
		List<IMessageEvent<?>> events = null;
		synchronized (clazzBoundEvents) {
			events = clazzBoundEvents.get(clazz);
		}
		if (events != null) {
			out.addAll(events);
		}
		return out;
	}

	/**
	 * 
	 * @return a {@link ArrayList} containing all {@link IMessageEvent}s that are not bound to a specific class.<br>
	 * If no non-bound events are registered an empty list will be returned.
	 */
	public List<IMessageEvent<?>> getEvents() {
		synchronized (nonboundEvents) {
			return new ArrayList<IMessageEvent<?>>(nonboundEvents);
		}
	}
	
	/**
	 * This method fires all {@link IMessageEvent}s that are registered for the object's {@link Class}
	 * and all non-bound events.
	 * @param object
	 */
	public void fireMessageEvents(IMessage object) {
		final List<IMessageEvent<?>> classBoundedEvents = getEvents(object.getClass());
		if (classBoundedEvents != null) {
			for (IMessageEvent<?> event : classBoundedEvents) {
				event.onReceived(CastUtil.cast(object));
			}
		}
		final List<IMessageEvent<?>> nonBoundedEvents = getEvents();
		if (nonBoundedEvents != null) {
			for (IMessageEvent<?> event : nonBoundedEvents) {
				event.onReceived(CastUtil.cast(object));
			}
		}
	}

	/**
	 * This method is called when a {@link ISession} gets inactive.
	 * @param session
	 */
	public void onSessionInactive(ISession session) { }
}
