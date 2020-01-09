package ru.usedesk.chat_sdk.external;

import java.util.List;

import ru.usedesk.chat_sdk.external.entity.Feedback;
import ru.usedesk.chat_sdk.external.entity.MessageButtons;
import ru.usedesk.chat_sdk.external.entity.OfflineForm;
import ru.usedesk.chat_sdk.external.entity.UsedeskFileInfo;
import ru.usedesk.common_sdk.external.entity.exceptions.UsedeskException;

public interface IUsedeskChatSdk {
    void sendMessage(String text) throws UsedeskException;

    void sendMessage(UsedeskFileInfo usedeskFileInfo) throws UsedeskException;

    void sendMessage(List<UsedeskFileInfo> usedeskFileInfoList) throws UsedeskException;

    void sendFeedbackMessage(Feedback feedback) throws UsedeskException;

    void sendOfflineForm(OfflineForm offlineForm) throws UsedeskException;

    void onClickButtonWidget(MessageButtons.MessageButton messageButton) throws UsedeskException;

    void disconnect() throws UsedeskException;
}
