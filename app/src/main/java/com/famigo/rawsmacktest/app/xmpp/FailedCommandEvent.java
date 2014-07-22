package com.famigo.rawsmacktest.app.xmpp;

/**
 * Created by adam.fitzgerald on 7/22/14.
 */
public class FailedCommandEvent {
    public final XMPPCommand command;

    public FailedCommandEvent(XMPPCommand cmd) {
        command = cmd;
    }
}
