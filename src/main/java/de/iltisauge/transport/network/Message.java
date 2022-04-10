package de.iltisauge.transport.network;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.iltisauge.transport.Transport;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * This class implements the {@link IMessage} interface.<br>
 * It contains methods to handle channels and to send the message.<br>
 * If you want to create your own message, you have to extend this class.
 * 
 * @author Daniel Ziegler
 *
 */
@Getter
@Setter
@ToString
public class Message implements IMessage {
	
	private final List<String> channels = new ArrayList<String>();
	private ISession from;
	private boolean receiveSelf;
	
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
	 * Sends this Message to the NetworkServer that will forward it to all clients that have registered the given channels.
	 */
	@Override
	public boolean send(String... channels) {
		return Transport.getClient().send(this, channels);
	}

	/**
	 * @returns a new <code>{@link String[]}</code> from the channel list.
	 */
	@Override
	public String[] getChannels() {
		return channels.toArray(new String[channels.size()]);
	}
}
