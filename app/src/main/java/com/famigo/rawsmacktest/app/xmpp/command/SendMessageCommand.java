package com.famigo.rawsmacktest.app.xmpp.command;

import android.util.Log;

import com.famigo.rawsmacktest.app.xmpp.AbsXMPPCommand;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.receipts.DeliveryReceiptManager;

/**
 * Created by adam.fitzgerald on 7/21/14.
 */
public class SendMessageCommand extends AbsXMPPCommand {

    private static final String TAG = SendMessageCommand.class.getSimpleName();
    private final Message mMessage;

    public SendMessageCommand( Message message ){
        this.mMessage = message;
    }

    @Override
    public void executeCommand(XMPPConnection activeConnection) {
        try {

            DeliveryReceiptManager.addDeliveryReceiptRequest(mMessage);
            activeConnection.sendPacket(mMessage);
        } catch (SmackException.NotConnectedException e) {
            Log.e(TAG, "cannot send mMessage", e);
        }
    }

    @Override
    public String getId() {
        return mMessage.getPacketID();
    }
}
