package ru.usedesk.sdk.external;

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
import ru.usedesk.sdk.internal.domain.interactor.ChatInteractor;

public class UsedeskChat {

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

    @Deprecated
    public void sendMessage(String text, UsedeskFile usedeskFile) {
        chatInteractor.sendUserMessage(text, usedeskFile);
    }

    @Deprecated
    public void sendMessage(String text, List<UsedeskFile> usedeskFiles) {
        chatInteractor.sendUserMessage(text, usedeskFiles);
    }

    public void sendTextMessage(String text) {
        chatInteractor.sendUserTextMessage(text);
    }

    @Deprecated
    public void sendFileMessage(UsedeskFile usedeskFile) {
        chatInteractor.sendUserFileMessage(usedeskFile);
    }

    public void sendFileMessage(@NonNull UsedeskFileInfo usedeskFileInfoList) {
        chatInteractor.sendUserFileMessage(usedeskFileInfoList);
    }

    public void sendFeedbackMessage(Feedback feedback) {
        chatInteractor.sendFeedbackMessage(feedback);
    }

    public void sendOfflineForm(OfflineForm offlineForm) {
        chatInteractor.sendOfflineForm(offlineForm);
    }

    public UsedeskConfiguration getUsedeskConfiguration() {
        return chatInteractor.getUsedeskConfiguration();
    }

    public void onClickButtonWidget(@NonNull MessageButtons.MessageButton messageButton) {
        chatInteractor.onClickButtonWidget(messageButton);
    }

    public void sendFileMessages(List<UsedeskFileInfo> usedeskFileInfoList) {
        if (usedeskFileInfoList == null || usedeskFileInfoList.size() == 0) {
            return;
        }

        for (UsedeskFileInfo usedeskFileInfo : usedeskFileInfoList) {
            sendFileMessage(usedeskFileInfo);
        }
    }
}