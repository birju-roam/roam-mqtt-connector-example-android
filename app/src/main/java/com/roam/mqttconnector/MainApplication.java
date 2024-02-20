package com.roam.mqttconnector;

import android.app.Application;

import com.roam.sdk.Roam;

public class MainApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Roam.initialize(this, "ROAM-PUBLISH-KEY");
    }
}
