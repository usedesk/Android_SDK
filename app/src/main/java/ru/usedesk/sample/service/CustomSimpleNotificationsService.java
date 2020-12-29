package ru.usedesk.sample.service;

import android.app.PendingIntent;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import ru.usedesk.chat_sdk.service.notifications.UsedeskNotificationsServiceFactory;
import ru.usedesk.chat_sdk.service.notifications.view.UsedeskSimpleNotificationsService;
import ru.usedesk.sample.ui.main.MainActivity;

public class CustomSimpleNotificationsService extends UsedeskSimpleNotificationsService {

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

    public static class Factory extends UsedeskNotificationsServiceFactory {
        @NonNull
        @Override
        protected Class<?> getServiceClass() {
            return CustomSimpleNotificationsService.class;
        }
    }
}
