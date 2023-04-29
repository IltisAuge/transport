package de.iltisauge.transport.messages;

import de.iltisauge.transport.network.IMessageCodec;
import de.iltisauge.transport.network.Message;
import de.iltisauge.transport.utils.PacketUtil;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;

/**
 * This {@link Message} is used to handle channel subscriptions on the server.
 * 
 * @author Daniel Ziegler
 *
 */
@Getter
@Setter
public class HandleSubscriptionsMessage extends Message {
	
	private HandleSubscriptionType handleSubscriptionType;
	private String[] channelsToSubscribe;
	
	public HandleSubscriptionsMessage(HandleSubscriptionType handleSubscriptionType, String... channels) {
		this.handleSubscriptionType = handleSubscriptionType;
		this.channelsToSubscribe = channels;
	}

	public static final IMessageCodec<HandleSubscriptionsMessage> CODEC = new IMessageCodec<HandleSubscriptionsMessage>() {
		
		@Override
		public void write(ByteBuf byteBuf, HandleSubscriptionsMessage obj) {
			PacketUtil.writeString(byteBuf, obj.getHandleSubscriptionType().name());
			byteBuf.writeInt(obj.getChannelsToSubscribe().length);
			for (String channel : obj.getChannelsToSubscribe()) {
				PacketUtil.writeString(byteBuf, channel);
			}
		}
		
		@Override
		public HandleSubscriptionsMessage read(ByteBuf byteBuf) {
			final HandleSubscriptionType handleSubscriptionType = HandleSubscriptionType.valueOf(PacketUtil.readString(byteBuf));
			final int length = byteBuf.readInt();
			final String[] channels = new String[length];
			for (int i = 0; i < channels.length; i++) {
				channels[i] = PacketUtil.readString(byteBuf);
			}
			return new HandleSubscriptionsMessage(handleSubscriptionType, channels);
		}
	};
	
	public enum HandleSubscriptionType {
		
		ADD,
		REMOVE;
	}
}
