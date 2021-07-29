package de.iltisauge.transport.network;

public interface IMessageEvent<T> {
	
	default void onReceived(T packet) {
	}
	
	default void onSent(T packet) {
	}
}
