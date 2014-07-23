package com.famigo.rawsmacktest.app.xmpp;

import org.jivesoftware.smack.XMPPConnection;

import java.lang.ref.WeakReference;
import java.util.Map;

/**
 * Created by adam.fitzgerald on 7/21/14.
 */
public abstract class XMPPCommand implements Runnable{

    private WeakReference<ICommandContext> weakCommandContext;
    public long expiration;

    public abstract void executeCommand(XMPPConnection activeConnection);
    public abstract String getId();

    public void initialize(ICommandContext context) {
        weakCommandContext = new WeakReference<ICommandContext>(context);
    }

    @Override
    public void run() {
        ICommandContext ctx = weakCommandContext.get();
        XMPPConnection activeConnection = ctx.getActiveConnection();
        if ( activeConnection != null && activeConnection.isConnected() && activeConnection.isAuthenticated() ){
            executeCommand(activeConnection);
        }
        ctx.addOutstandingCommand(this);
    }


}
