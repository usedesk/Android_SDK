package ru.usedesk.sample.service;

import android.app.PendingIntent;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ru.usedesk.chat_sdk.external.service.notifications.UsedeskNotificationsServiceFactory;
import ru.usedesk.chat_sdk.external.service.notifications.view.UsedeskForegroundNotificationsService;
import ru.usedesk.sample.ui.main.MainActivity;

public class CustomForegroundNotificationsService extends UsedeskForegroundNotificationsService {

    private static final int FOREGROUND_ID = 137;

    @Nullable
    @Override
    protected PendingIntent getContentPendingIntent() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return PendingIntent.getActivity(this, 0, intent, 0);
    }

    @Nullable
    @Override
    protected PendingIntent getDeletePendingIntent() {
        return null;
    }

    @Override
    protected int getForegroundId() {
        return FOREGROUND_ID;
    }

    public static class Factory extends UsedeskNotificationsServiceFactory {
        @NonNull
        @Override
        protected Class<?> getServiceClass() {
            return CustomForegroundNotificationsService.class;
        }
    }
}
