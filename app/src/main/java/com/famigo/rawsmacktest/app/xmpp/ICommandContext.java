package com.famigo.rawsmacktest.app.xmpp;

import org.jivesoftware.smack.XMPPConnection;

import java.util.Collection;

/**
 * Created by adam.fitzgerald on 7/22/14.
 */
public interface ICommandContext {

    public XMPPConnection getActiveConnection();
    public void addOutstandingCommand( XMPPCommand command );

    java.util.Map<String, XMPPCommand> getOutStandingCommands();

    public void postOnMain(final Object event);

    public void watchOutstandingCommands();

    Collection<String> getServicedPackets();
}
