package com.famigo.rawsmacktest.app.xmpp;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Packet;

import java.util.Collection;

/**
 * Created by adam.fitzgerald on 7/23/14.
 */
public abstract class PacketHandler implements PacketListener {

    protected ICommandContext ctx;

    public abstract PacketFilter getFilter();
    public abstract XMPPEvent handlePacket(Packet packet);

    public PacketHandler(ICommandContext context) {
        ctx = context;
    }

    @Override
    public void processPacket(Packet packet) throws SmackException.NotConnectedException {
        Collection<String> servicedPackets = ctx.getServicedPackets();
        if ( !servicedPackets.contains(packet.getPacketID())) {
            servicedPackets.add(packet.getPacketID());
            ctx.postOnMain(handlePacket(packet));
        }
    }

}
