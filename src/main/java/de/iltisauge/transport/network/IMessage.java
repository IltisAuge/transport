package de.iltisauge.transport.network;

/**
 * This interface extends {@link Sendable} and can be used to create message classes.
 * 
 * @author Daniel Ziegler
 *
 */
public interface IMessage extends Sendable {
	
	boolean send(String... channels);
}