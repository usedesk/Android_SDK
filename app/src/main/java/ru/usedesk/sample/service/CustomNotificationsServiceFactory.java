package ru.usedesk.sample.service;

import android.support.annotation.NonNull;

import ru.usedesk.sdk.external.service.notifications.UsedeskNotificationsServiceFactory;

public class CustomNotificationsServiceFactory extends UsedeskNotificationsServiceFactory {
    @NonNull
    @Override
    protected Class<?> getServiceClass() {
        return CustomNotificationsService.class;
    }
}
