package com.famigo.rawsmacktest.app.xmpp.command;

import com.famigo.rawsmacktest.app.xmpp.XMPPCommand;

import org.jivesoftware.smack.XMPPConnection;

/**
 * Created by adam.fitzgerald on 7/22/14.
 */
public class RetryCommand extends XMPPCommand{

    private final XMPPCommand originalCommand;

    public int tries = 0;
    public long retryAfter;

    public RetryCommand(XMPPCommand command) {
        this.originalCommand = command;
    }

    @Override
    public void executeCommand(XMPPConnection activeConnection) {
        originalCommand.executeCommand(activeConnection);
    }

    @Override
    public String getId() {
        return originalCommand.getId();
    }
}
