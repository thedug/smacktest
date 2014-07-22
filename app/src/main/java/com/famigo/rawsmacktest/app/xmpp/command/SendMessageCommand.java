package com.famigo.rawsmacktest.app.xmpp.command;

import android.util.Log;

import com.famigo.rawsmacktest.app.xmpp.XMPPCommand;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Message;

/**
 * Created by adam.fitzgerald on 7/21/14.
 */
public class SendMessageCommand extends XMPPCommand{

    private static final String TAG = SendMessageCommand.class.getSimpleName();
    private final Message message;

    public SendMessageCommand( Message message ){
        this.message = message;
    }

    @Override
    protected void executeCommand(XMPPConnection activeConnection) {
        try {
            activeConnection.sendPacket(message);
        } catch (SmackException.NotConnectedException e) {
            Log.e(TAG, "cannot send message", e);
        }
    }
}
