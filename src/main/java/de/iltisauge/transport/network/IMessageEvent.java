package de.iltisauge.transport.network;

public interface IMessageEvent<T> {
	
	default void onReceived(T message) {
	}
	
	default void onSent(T message) {
	}
}
