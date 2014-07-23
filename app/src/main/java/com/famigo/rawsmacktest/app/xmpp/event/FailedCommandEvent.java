package com.famigo.rawsmacktest.app.xmpp.event;

import com.famigo.rawsmacktest.app.xmpp.AbsXMPPCommand;
import com.famigo.rawsmacktest.app.xmpp.IXMPPEvent;

/**
 * Created by adam.fitzgerald on 7/22/14.
 */
public class FailedCommandEvent implements IXMPPEvent {
    public final AbsXMPPCommand mCommand;

    public FailedCommandEvent(AbsXMPPCommand cmd) {
        mCommand = cmd;
    }
}
