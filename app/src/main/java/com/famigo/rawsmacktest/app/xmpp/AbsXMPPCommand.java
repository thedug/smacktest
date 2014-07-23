package com.famigo.rawsmacktest.app.xmpp;

import org.jivesoftware.smack.XMPPConnection;

import java.lang.ref.WeakReference;

/**
 * Created by adam.fitzgerald on 7/21/14.
 */
public abstract class AbsXMPPCommand implements Runnable{

    private WeakReference<IXMPPContext> mWeakCommandContext;
    public long mExpiration;

    public abstract void executeCommand(XMPPConnection activeConnection);
    public abstract String getId();

    public void initialize(IXMPPContext context) {
        mWeakCommandContext = new WeakReference<IXMPPContext>(context);
    }

    @Override
    public void run() {
        IXMPPContext ctx = mWeakCommandContext.get();
        XMPPConnection activeConnection = ctx.getActiveConnection();
        if ( activeConnection != null && activeConnection.isConnected() && activeConnection.isAuthenticated() ){
            executeCommand(activeConnection);
        }
        ctx.addOutstandingCommand(this);
    }


}
