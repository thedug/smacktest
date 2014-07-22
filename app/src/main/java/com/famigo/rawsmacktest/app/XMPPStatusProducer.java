package com.famigo.rawsmacktest.app;

import android.util.Log;

import com.famigo.rawsmacktest.app.xmpp.event.XMPPStatusEvent;
import com.squareup.otto.Produce;
import com.squareup.otto.Subscribe;

/**
 * Created by adam.fitzgerald on 7/21/14.
 */
public class XMPPStatusProducer {

    private static final String TAG = XMPPStatusProducer.class.getSimpleName();
    XMPPStatusEvent lastEvent = XMPPStatusEvent.UNINITIALIZED;

    @Subscribe
    public void onUpdate( XMPPStatusEvent event ){
        Log.i(TAG, String.format("transition from: %s to: %s", lastEvent.name(), event));
        lastEvent = event;
    }

    @Produce
    public XMPPStatusEvent produceEvent(){
        return lastEvent;
    }


}
