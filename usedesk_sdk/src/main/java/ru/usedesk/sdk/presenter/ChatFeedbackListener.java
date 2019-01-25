package ru.usedesk.sdk.presenter;

import ru.usedesk.sdk.domain.entity.Feedback;

public interface ChatFeedbackListener {

    void onFeedbackSet(Feedback feedback);
}