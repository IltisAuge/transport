package de.iltisauge.transport.network;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@ToString
public class Message implements IMessage {

	@Setter
	private ISession from;
	private final List<String> channels = new ArrayList<String>();
	
	@Override
	public void addChannels(String... channels) {
		this.channels.addAll(Arrays.asList(channels));
	}
	
	@Override
	public boolean send(String... channels) {
		//return PacketTransport.getAPI().getNetworkClient().send(this, channels);
		return true;
	}
}