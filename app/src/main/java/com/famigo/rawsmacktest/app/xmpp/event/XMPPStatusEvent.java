package com.famigo.rawsmacktest.app.xmpp.event;

import com.famigo.rawsmacktest.app.xmpp.IXMPPEvent;

/**
 * Created by adam.fitzgerald on 7/21/14.
 */
public enum XMPPStatusEvent implements IXMPPEvent {

    CONNECTED, AUTHENTICATED, CONNECTION_CLOSED,
    CONNECTION_CLOSED_ERROR, RECONNECTED, RECONNECT_FAILED,
    RECONNECTING, UNINITIALIZED
}
