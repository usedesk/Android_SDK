package ru.usedesk.sample;

import androidx.multidex.MultiDexApplication;

public class App extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();

        ServiceLocator.init(this);
    }
}
