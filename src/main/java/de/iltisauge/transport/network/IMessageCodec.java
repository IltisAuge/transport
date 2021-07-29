package de.iltisauge.transport.network;

import io.netty.buffer.ByteBuf;

public interface IMessageCodec<T> {
	
	void write(ByteBuf byteBuf, T obj);
	
	T read(ByteBuf byteBuf);

}
