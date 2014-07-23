package com.famigo.rawsmacktest.app;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

/**
 * Created by adam.fitzgerald on 7/21/14.
 *
 * A bus is an integral part of this XMPP system
 * In the future this singleton should be replaced by a DI tool
 *
 */
public class BusProvider {

    private static Bus mBus = new Bus(ThreadEnforcer.MAIN);

    /*
     * registers the status provider with teh buss
     * Needed if you care about getting the current state of the XMPP server
     * without waiting for a change in status to be broadcasted
     */
    static {
        mBus.register(new XMPPStatusProducer());
    }

    public static Bus getBus() {
        return mBus;
    }
}
