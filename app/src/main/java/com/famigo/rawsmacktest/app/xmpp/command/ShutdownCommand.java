package com.famigo.rawsmacktest.app.xmpp.command;

import android.util.Log;

import com.famigo.rawsmacktest.app.xmpp.XMPPCommand;

import org.jivesoftware.smack.XMPPConnection;

/**
 * Created by adam.fitzgerald on 7/23/14.
 */
public class ShutdownCommand extends XMPPCommand {

    private static final String TAG = ShutdownCommand.class.getSimpleName();

    @Override
    public void executeCommand(XMPPConnection activeConnection) {
        try {
            activeConnection.disconnect();
        } catch (Exception e) {
            Log.e(TAG, "cannot shutdown?", e);
        }
    }

    @Override
    public String getId() {
        return "shutdown";
    }
}
