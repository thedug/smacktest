package com.famigo.rawsmacktest.app.xmpp.command;

import android.util.Log;

import com.famigo.rawsmacktest.app.xmpp.XMPPCommand;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.receipts.DeliveryReceiptManager;

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
    public void executeCommand(XMPPConnection activeConnection) {
        try {

            DeliveryReceiptManager.addDeliveryReceiptRequest(message);
            activeConnection.sendPacket(message);
        } catch (SmackException.NotConnectedException e) {
            Log.e(TAG, "cannot send message", e);
        }
    }

    @Override
    public String getId() {
        return message.getPacketID();
    }
}
