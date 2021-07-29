package de.iltisauge.transport.network;

import java.util.List;

public interface IMessage {
	
	ISession getFrom();
	
	void setFrom(ISession session);

	List<String> getChannels();
	
	void addChannels(String... channels);
	
	boolean send(String... channels);
}