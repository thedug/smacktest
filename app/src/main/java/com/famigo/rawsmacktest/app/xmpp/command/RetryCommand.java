package com.famigo.rawsmacktest.app.xmpp.command;

import com.famigo.rawsmacktest.app.xmpp.AbsXMPPCommand;

import org.jivesoftware.smack.XMPPConnection;

/**
 * Created by adam.fitzgerald on 7/22/14.
 */
public class RetryCommand extends AbsXMPPCommand {

    private final AbsXMPPCommand mOriginalCommand;

    public int mTries = 0;
    public long mRetryAfter;

    public RetryCommand(AbsXMPPCommand command) {
        this.mOriginalCommand = command;
    }

    @Override
    public void executeCommand(XMPPConnection activeConnection) {
        mOriginalCommand.executeCommand(activeConnection);
    }

    @Override
    public String getId() {
        return mOriginalCommand.getId();
    }
}
