package com.famigo.rawsmacktest.app.xmpp;

import org.jivesoftware.smack.XMPPConnection;

import java.util.Collection;

/**
 * Created by adam.fitzgerald on 7/22/14.
 */
public interface IXMPPContext {

    public XMPPConnection getmActiveConnection();
    public void addOutstandingCommand( AbsXMPPCommand command );

    java.util.Map<String, AbsXMPPCommand> getmOutStandingCommands();

    public void postOnMain(final Object event);

    public void watchOutstandingCommands();

    Collection<String> getmServicedPackets();
}
