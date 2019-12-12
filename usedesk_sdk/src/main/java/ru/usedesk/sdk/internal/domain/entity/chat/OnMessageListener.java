package ru.usedesk.sdk.internal.domain.entity.chat;

import ru.usedesk.sdk.external.entity.chat.Message;
import ru.usedesk.sdk.internal.data.framework.api.standard.entity.response.Setup;

public interface OnMessageListener {

    void onNew(Message message);

    void onFeedback();

    void onInit(String token, Setup setup);

    void onInitChat();

    void onTokenError();
}
