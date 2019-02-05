package ru.usedesk.sdk.domain.entity;

import ru.usedesk.sdk.data.framework.entity.response.ErrorResponse;
import ru.usedesk.sdk.data.framework.entity.response.InitChatResponse;
import ru.usedesk.sdk.data.framework.entity.response.NewMessageResponse;
import ru.usedesk.sdk.data.framework.entity.response.SendFeedbackResponse;

public interface OnMessageListener {

    void onNew(NewMessageResponse newMessageResponse);

    void onFeedback(SendFeedbackResponse response);

    void onError(ErrorResponse response);

    void onInit(InitChatResponse response);

    void onInitChat();
}
