package com.famigo.rawsmacktest.app;

import android.app.Application;

import org.jivesoftware.smack.SmackAndroid;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smackx.receipts.DeliveryReceipt;
import org.jivesoftware.smackx.receipts.DeliveryReceiptRequest;

/**
 * Created by adam.fitzgerald on 7/18/14.
 */
public class TestApplication extends Application{

    @Override
    public void onCreate() {
        super.onCreate();

        /*
         * USAGE NOTE!
         * this code must be run before XMPP will function correctly
         */
        SmackAndroid.init(this);
        ProviderManager.addExtensionProvider(DeliveryReceipt.ELEMENT, DeliveryReceipt.NAMESPACE, new DeliveryReceipt.Provider());
        ProviderManager.addExtensionProvider(DeliveryReceiptRequest.ELEMENT, new DeliveryReceiptRequest().getNamespace(), new DeliveryReceiptRequest.Provider());

    }
}
