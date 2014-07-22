package com.famigo.rawsmacktest.app.xmpp.event;

import com.famigo.rawsmacktest.app.xmpp.XMPPEvent;

import org.jivesoftware.smack.packet.Message;

/**
 * Created by adam.fitzgerald on 7/21/14.
 */
public class IncommingMessage implements XMPPEvent {

    private Message message;

    public IncommingMessage(Message message) {
        this.message = message;
    }

    public Message getMessage() {
        return message;
    }
}
