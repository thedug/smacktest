package com.famigo.rawsmacktest.app.xmpp.event;

import com.famigo.rawsmacktest.app.xmpp.IXMPPEvent;

import org.jivesoftware.smack.packet.Message;

/**
 * Created by adam.fitzgerald on 7/21/14.
 */
public class IncommingMessage implements IXMPPEvent {

    private Message mMessage;

    public IncommingMessage(Message message) {
        this.mMessage = message;
    }

    public Message getmMessage() {
        return mMessage;
    }
}
