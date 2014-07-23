package com.famigo.rawsmacktest.app.xmpp;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Packet;

import java.util.Collection;

/**
 * Created by adam.fitzgerald on 7/23/14.
 */
public abstract class AbsPacketHandler implements PacketListener {

    protected IXMPPContext mCtx;

    public abstract PacketFilter getFilter();
    public abstract IXMPPEvent handlePacket(Packet packet);

    public AbsPacketHandler(IXMPPContext context) {
        mCtx = context;
    }

    @Override
    public void processPacket(Packet packet) throws SmackException.NotConnectedException {
        Collection<String> servicedPackets = mCtx.getmServicedPackets();
        if ( !servicedPackets.contains(packet.getPacketID())) {
            servicedPackets.add(packet.getPacketID());
            mCtx.postOnMain(handlePacket(packet));
        }
    }

}
