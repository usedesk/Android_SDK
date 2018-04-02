package ru.usedesk.sdk;

import ru.usedesk.sdk.models.Feedback;

public interface ChatFeedbackListener {

    void onFeedbackSet(Feedback feedback);
}