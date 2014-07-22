package com.famigo.rawsmacktest.app.xmpp.event;

import com.famigo.rawsmacktest.app.xmpp.XMPPCommand;
import com.famigo.rawsmacktest.app.xmpp.XMPPEvent;

/**
 * Created by adam.fitzgerald on 7/22/14.
 */
public class FailedCommandEvent implements XMPPEvent{
    public final XMPPCommand command;

    public FailedCommandEvent(XMPPCommand cmd) {
        command = cmd;
    }
}
