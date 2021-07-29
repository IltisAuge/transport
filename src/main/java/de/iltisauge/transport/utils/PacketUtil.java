package de.iltisauge.transport.utils;

import java.nio.charset.Charset;

import io.netty.buffer.ByteBuf;

public class PacketUtil {
	
	public static void writeString(ByteBuf byteBuf, String string) {
		byteBuf.writeInt(string.length());
		byteBuf.writeBytes(string.getBytes());
	}
	
	public static String readString(ByteBuf byteBuf) {
		final int length = byteBuf.readInt();
		final byte[] bytes = new byte[length];
		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = byteBuf.readByte();
		}
		return new String(bytes, Charset.forName("UTF-8"));
	}
}
