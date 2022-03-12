package de.iltisauge.transport.network;

/**
 * MessageEvents are called when {@link IMessage} have been sent or received.
 * 
 * @author Daniel Ziegler
 *
 * @param <T>
 */
public interface IMessageEvent<T> {
	
	default void onReceived(T message) {
	}
	
	default void onSent(T message) {
	}
}
