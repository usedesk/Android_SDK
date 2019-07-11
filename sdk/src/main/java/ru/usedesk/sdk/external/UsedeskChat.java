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
import ru.usedesk.sdk.internal.domain.interactor.ChatInteractor;

@SuppressWarnings("Injectable")
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

    public void destroy() {
        if (chatInteractor != null) {
            chatInteractor.disconnect();
        }
        chatInteractor = null;
    }

    public void sendMessage(String text, UsedeskFile usedeskFile) {
        chatInteractor.sendUserMessage(text, usedeskFile);
    }

    public void sendMessage(String text, List<UsedeskFile> usedeskFiles) {
        chatInteractor.sendUserMessage(text, usedeskFiles);
    }

    public void sendTextMessage(String text) {
        chatInteractor.sendUserTextMessage(text);
    }

    public void sendFileMessage(UsedeskFile usedeskFile) {
        chatInteractor.sendUserFileMessage(usedeskFile);
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
}