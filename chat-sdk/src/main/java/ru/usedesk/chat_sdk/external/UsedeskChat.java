package ru.usedesk.chat_sdk.external;

import android.support.annotation.NonNull;

import java.util.List;

import javax.inject.Inject;

import ru.usedesk.sdk.external.entity.chat.Feedback;
import ru.usedesk.sdk.external.entity.chat.MessageButtons;
import ru.usedesk.sdk.external.entity.chat.OfflineForm;
import ru.usedesk.sdk.external.entity.chat.UsedeskActionListener;
import ru.usedesk.sdk.external.entity.chat.UsedeskConfiguration;
import ru.usedesk.sdk.external.entity.chat.UsedeskFile;
import ru.usedesk.sdk.external.entity.chat.UsedeskFileInfo;

@Deprecated
public class UsedeskChat {//TODO: convert to UsedeskSdkBox

    private ChatInteractor chatInteractor;

    @Inject
    UsedeskChat(@NonNull ChatInteractor chatInteractor) {
        this.chatInteractor = chatInteractor;
    }

    void init(@NonNull UsedeskConfiguration configuration,
              @NonNull UsedeskActionListener actionListener) {
        chatInteractor.init(configuration, actionListener);
    }

    void destroy() {
        if (chatInteractor != null) {
            chatInteractor.disconnect();
        }
        chatInteractor = null;
    }

    public void sendTextMessage(String text) {
        chatInteractor.sendUserTextMessage(text);
    }


    public void sendFileMessage(@NonNull UsedeskFileInfo usedeskFileInfoList) {
        chatInteractor.sendUserFileMessage(usedeskFileInfoList);
    }

    public void sendFileMessages(List<UsedeskFileInfo> usedeskFileInfoList) {
        if (usedeskFileInfoList == null) {
            return;
        }

        for (UsedeskFileInfo usedeskFileInfo : usedeskFileInfoList) {
            sendFileMessage(usedeskFileInfo);
        }
    }

    public void sendFeedbackMessage(Feedback feedback) {
        chatInteractor.sendFeedbackMessage(feedback);
    }

    public UsedeskConfiguration getUsedeskConfiguration() {
        return chatInteractor.getUsedeskConfiguration();
    }

    public void onClickButtonWidget(@NonNull MessageButtons.MessageButton messageButton) {
        chatInteractor.onClickButtonWidget(messageButton);
    }

    @Deprecated
    public void sendOfflineForm(OfflineForm offlineForm) {
        chatInteractor.sendOfflineFormSync(offlineForm);
    }

    @Deprecated
    public void sendFileMessage(UsedeskFile usedeskFile) {
        chatInteractor.sendUserFileMessage(usedeskFile);
    }

    @Deprecated
    public void sendMessage(String text, UsedeskFile usedeskFile) {
        chatInteractor.sendUserMessage(text, usedeskFile);
    }

    @Deprecated
    public void sendMessage(String text, List<UsedeskFile> usedeskFiles) {
        chatInteractor.sendUserMessage(text, usedeskFiles);
    }
}