package com.famigo.rawsmacktest.app.xmpp;

import org.jivesoftware.smack.XMPPConnection;

import java.lang.ref.WeakReference;

/**
 * Created by adam.fitzgerald on 7/21/14.
 */
public abstract class XMPPCommand implements Runnable{

    private WeakReference<IXMPPContext> weakCommandContext;
    public long expiration;

    public abstract void executeCommand(XMPPConnection activeConnection);
    public abstract String getId();

    public void initialize(IXMPPContext context) {
        weakCommandContext = new WeakReference<IXMPPContext>(context);
    }

    @Override
    public void run() {
        IXMPPContext ctx = weakCommandContext.get();
        XMPPConnection activeConnection = ctx.getActiveConnection();
        if ( activeConnection != null && activeConnection.isConnected() && activeConnection.isAuthenticated() ){
            executeCommand(activeConnection);
        }
        ctx.addOutstandingCommand(this);
    }


}
