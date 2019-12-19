package ru.usedesk.sample;

import android.app.Application;
import android.support.annotation.NonNull;

public class App extends Application {

    private static App instance;

    @NonNull
    public static App getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;
    }
}
