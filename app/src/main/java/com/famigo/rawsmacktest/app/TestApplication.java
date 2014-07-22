package com.famigo.rawsmacktest.app;

import android.app.Application;

import org.jivesoftware.smack.SmackAndroid;

/**
 * Created by adam.fitzgerald on 7/18/14.
 */
public class TestApplication extends Application{

    @Override
    public void onCreate() {
        super.onCreate();

        SmackAndroid.init(this);

    }
}
