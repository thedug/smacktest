package com.famigo.rawsmacktest.app.xmpp;

import android.os.SystemClock;
import android.util.Log;

import com.famigo.rawsmacktest.app.xmpp.event.FailedCommandEvent;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by adam.fitzgerald on 7/22/14.
 */
public class OutstandingCheckTask implements  Runnable {

    private static final String TAG = OutstandingCheckTask.class.getSimpleName();

    private final WeakReference<IXMPPContext> mWeakCommandContext;

    public OutstandingCheckTask(IXMPPContext ctx) {
        mWeakCommandContext = new WeakReference<IXMPPContext>(ctx);
    }

    @Override
    public void run() {
        IXMPPContext ctx = mWeakCommandContext.get();
        if( ctx != null ){
            Log.i(TAG, "checking out standing commands");
            Iterator<Map.Entry<String, AbsXMPPCommand>> iter = ctx.getmOutStandingCommands().entrySet().iterator();
            long nao = SystemClock.uptimeMillis();
            while( iter.hasNext() ){
                AbsXMPPCommand cmd = iter.next().getValue();
                if ( cmd.mExpiration <= nao ){
                    iter.remove();
                    ctx.postOnMain(new FailedCommandEvent(cmd));
                }
            }
        }
        ctx.watchOutstandingCommands();
    }
}
