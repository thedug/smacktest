package com.famigo.rawsmacktest.app;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

/**
 * Created by adam.fitzgerald on 7/21/14.
 */
public class BusProvider {

    private static Bus bus = new Bus(ThreadEnforcer.MAIN);

    static {
        bus.register(new XMPPStatusProducer());
    }

    public static Bus getBus() {
        return bus;
    }
}
