package com.famigo.rawsmacktest.app.xmpp;

import org.jivesoftware.smack.XMPPConnection;

import java.lang.ref.WeakReference;

/**
 * Created by adam.fitzgerald on 7/21/14.
 */
public abstract class XMPPCommand implements Runnable{

    private WeakReference<XMPPConnection> weakActiveConnection;

    protected abstract void executeCommand(XMPPConnection activeConnection);

    public void applyConnection(XMPPConnection activeConnection) {
        weakActiveConnection = new WeakReference<XMPPConnection>(activeConnection);
    }

    @Override
    public void run() {
        XMPPConnection activeConnection = weakActiveConnection.get();
        if ( activeConnection != null && activeConnection.isConnected() && activeConnection.isAuthenticated() ){
            executeCommand(activeConnection);
        }
    }

}
