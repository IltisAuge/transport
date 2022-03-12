package de.iltisauge.transport.network;

import io.netty.buffer.ByteBuf;

/**
 * A IMessageCodec is used to encode and decode messages.
 * 
 * @author Daniel Ziegler
 *
 * @param <T>
 */
public interface IMessageCodec<T> {
	
	/**
	 * Writes an object to a {@link ByteBuf}.
	 * @param byteBuf
	 * @param obj
	 */
	void write(ByteBuf byteBuf, T obj);
	
	/**
	 * Reads an object from a {@link ByteBuf}.
	 * @param byteBuf
	 * @return the read object.
	 */
	T read(ByteBuf byteBuf);

}
