package ru.usedesk.sample;

import android.app.Application;

import ru.usedesk.sample.service.CustomNotificationsServiceFactory;
import ru.usedesk.sdk.external.UsedeskSdk;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        UsedeskSdk.setUsedeskNotificationsServiceFactory(new CustomNotificationsServiceFactory());
    }
}
