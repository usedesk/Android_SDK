package ru.usedesk.sdk.domain.entity;

import ru.usedesk.sdk.domain.entity.Feedback;

public interface ChatFeedbackListener {

    void onFeedbackSet(Feedback feedback);
}