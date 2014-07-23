package com.famigo.rawsmacktest.app.xmpp.packet_handler;

import com.famigo.rawsmacktest.app.xmpp.AbsPacketHandler;
import com.famigo.rawsmacktest.app.xmpp.IXMPPContext;
import com.famigo.rawsmacktest.app.xmpp.IXMPPEvent;
import com.famigo.rawsmacktest.app.xmpp.event.IncomingMessage;

import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;

/**
 * Created by adam.fitzgerald on 7/23/14.
 */
public class ChatMessageHandler extends AbsPacketHandler {

    public ChatMessageHandler(IXMPPContext context) {
        super(context);
    }

    @Override
    public PacketFilter getFilter() {
        return new MessageTypeFilter(Message.Type.chat);
    }

    @Override
    public IXMPPEvent handlePacket(Packet packet) {
        return new IncomingMessage((Message) packet);
    }


}
