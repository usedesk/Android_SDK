package ru.usedesk.chat_sdk.external;

import android.support.annotation.NonNull;

import ru.usedesk.chat_sdk.external.entity.Feedback;
import ru.usedesk.chat_sdk.external.entity.MessageButtons;
import ru.usedesk.chat_sdk.external.entity.OfflineForm;
import ru.usedesk.chat_sdk.external.entity.UsedeskActionListener;
import ru.usedesk.chat_sdk.external.entity.UsedeskChatConfiguration;
import ru.usedesk.chat_sdk.external.entity.UsedeskFileInfo;
import ru.usedesk.common_sdk.external.entity.exceptions.UsedeskHttpException;

public interface IUsedeskChatSdk {
    void init(@NonNull UsedeskChatConfiguration usedeskChatConfiguration,
              @NonNull UsedeskActionListener usedeskActionListener);

    void disconnect();

    void sendUserFileMessage(@NonNull UsedeskFileInfo usedeskFileInfo);

    void sendUserTextMessage(String text);

    void sendFeedbackMessage(Feedback feedback);

    void sendOfflineForm(OfflineForm offlineForm) throws UsedeskHttpException;

    void onClickButtonWidget(MessageButtons.MessageButton messageButton);
}
