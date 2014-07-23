package com.famigo.rawsmacktest.app.xmpp.packet_handler;

import com.famigo.rawsmacktest.app.xmpp.IXMPPContext;
import com.famigo.rawsmacktest.app.xmpp.PacketHandler;
import com.famigo.rawsmacktest.app.xmpp.XMPPEvent;
import com.famigo.rawsmacktest.app.xmpp.event.IncommingMessage;

import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;

/**
 * Created by adam.fitzgerald on 7/23/14.
 */
public class ChatMessageHandler extends PacketHandler {

    public ChatMessageHandler(IXMPPContext context) {
        super(context);
    }

    @Override
    public PacketFilter getFilter() {
        return new MessageTypeFilter(Message.Type.chat);
    }

    @Override
    public XMPPEvent handlePacket(Packet packet) {
        return new IncommingMessage((Message) packet);
    }


}
