package de.iltisauge.transport.messages;

import de.iltisauge.transport.network.IMessageCodec;
import de.iltisauge.transport.network.Message;
import de.iltisauge.transport.utils.PacketUtil;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * This {@link Message} is used to send some text.
 * 
 * @author Daniel Ziegler
 *
 */
@AllArgsConstructor
@Getter
@Setter
public class TextMessage extends Message {
	
	private String text;
	
	public static IMessageCodec<TextMessage> CODEC = new IMessageCodec<TextMessage>() {
		
		@Override
		public void write(ByteBuf byteBuf, TextMessage obj) {
			PacketUtil.writeString(byteBuf, obj.getText());
		}
		
		@Override
		public TextMessage read(ByteBuf byteBuf) {
			return new TextMessage(PacketUtil.readString(byteBuf));
		}
	};
}
