package de.iltisauge.transport.network;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@Setter
@Getter
public class ServerMessageWrapper implements Sendable {
	
	private ISession from;
	private boolean receiveSelf;
	private final List<String> channels = new ArrayList<String>();
	private final String className;
	/**
	 * Contains unread class codec.
	 */
	private final ByteBuf bufferCopy;
	
	public ByteBuf getBufferCopy() {
		return bufferCopy.copy();
	}

	/**
	 * Adds the channel array to the channel list.
	 */
	@Override
	public void addChannels(String... channels) {
		this.channels.addAll(Arrays.asList(channels));
	}

	/**
	 * Removes the channel array from the channel list.
	 */
	@Override
	public void removeChannels(String... channels) {
		this.channels.removeAll(Arrays.asList(channels));
	}

	/**
	 * @returns a new <code>{@link String[]}</code> from the channel list.
	 */
	@Override
	public String[] getChannels() {
		return channels.toArray(new String[channels.size()]);
	}
}
