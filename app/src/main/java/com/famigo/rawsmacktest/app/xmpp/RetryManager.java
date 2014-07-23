package com.famigo.rawsmacktest.app.xmpp;

import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;

import com.famigo.rawsmacktest.app.BusProvider;
import com.famigo.rawsmacktest.app.xmpp.command.RetryCommand;
import com.famigo.rawsmacktest.app.xmpp.event.FailedCommandEvent;
import com.squareup.otto.Subscribe;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by adam.fitzgerald on 7/22/14.
 */
public class RetryManager implements Runnable{

    private static final String TAG = RetryManager.class.getSimpleName();
    public static final int MAX_RETRIES = 3;
    private static final int BASE_DELAY = 10000;

    private final WeakReference<Handler> mWeakHandler;

    private Map<String, RetryCommand> mRetriesOutstanding = new ConcurrentHashMap<String, RetryCommand>();

    public RetryManager(Handler handler){
        this.mWeakHandler = new WeakReference<Handler>(handler);

        BusProvider.getmBus().register(this);

    }

    @Subscribe
    public void onFailedCommandEvent( FailedCommandEvent event ){
        RetryCommand retryCommand = null;
        if ( event.mCommand instanceof RetryCommand ){
            retryCommand = (RetryCommand) event.mCommand;
        } else {
            retryCommand = new RetryCommand(event.mCommand);
        }

        retryCommand.mRetryAfter = SystemClock.uptimeMillis() + BASE_DELAY * retryCommand.mTries;
        retryCommand.mTries++;

        if ( retryCommand.mTries <= MAX_RETRIES ){
            mRetriesOutstanding.put(retryCommand.getId(), retryCommand);
        }

    }

    public void removeLateBloomer(String id){
        mRetriesOutstanding.remove(id);
    }

    @Override
    public void run() {
        Log.i(TAG, String.format("we have %d potential retriesOutstanding scheduled", mRetriesOutstanding.size()));
        Iterator<Map.Entry<String, RetryCommand>> iter = mRetriesOutstanding.entrySet().iterator();
        while (iter.hasNext()) {
            RetryCommand cmd = iter.next().getValue();
            if (cmd.mRetryAfter <= SystemClock.uptimeMillis()) {
                BusProvider.getmBus().post(cmd);
                iter.remove();
            }
        }

        Handler handler = mWeakHandler.get();
        if (handler != null) {
            handler.postDelayed(this, BASE_DELAY / 2);
        }
    }

    public void shutdown(){
        Handler handler = mWeakHandler.get();
        if ( handler != null ) {
            handler.removeCallbacks(this);
        }
        BusProvider.getmBus().unregister(this);
        mRetriesOutstanding.clear();
    }
}
