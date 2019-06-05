package ru.usedesk.sample.service;

import android.app.PendingIntent;
import android.content.Intent;
import android.support.annotation.Nullable;

import ru.usedesk.sample.ui.main.MainActivity;
import ru.usedesk.sdk.external.service.notifications.UsedeskNotificationsService;

public class CustomNotificationsService extends UsedeskNotificationsService {

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
}
