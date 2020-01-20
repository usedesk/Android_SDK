package ru.usedesk.chat_sdk.internal.domain;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.Completable;
import ru.usedesk.chat_sdk.external.IUsedeskChatSdk;
import ru.usedesk.chat_sdk.external.entity.Feedback;
import ru.usedesk.chat_sdk.external.entity.Message;
import ru.usedesk.chat_sdk.external.entity.MessageButtons;
import ru.usedesk.chat_sdk.external.entity.MessageType;
import ru.usedesk.chat_sdk.external.entity.OfflineForm;
import ru.usedesk.chat_sdk.external.entity.OnMessageListener;
import ru.usedesk.chat_sdk.external.entity.UsedeskActionListener;
import ru.usedesk.chat_sdk.external.entity.UsedeskChatConfiguration;
import ru.usedesk.chat_sdk.external.entity.UsedeskFileInfo;
import ru.usedesk.chat_sdk.internal.data.framework.socket.entity.response.Setup;
import ru.usedesk.chat_sdk.internal.data.repository.api.IApiRepository;
import ru.usedesk.chat_sdk.internal.data.repository.configuration.IUserInfoRepository;
import ru.usedesk.common_sdk.external.entity.exceptions.UsedeskDataNotFoundException;
import ru.usedesk.common_sdk.external.entity.exceptions.UsedeskException;

public class ChatSdkInteractor implements IUsedeskChatSdk {

    private final Context context;
    private final UsedeskChatConfiguration configuration;
    private final UsedeskActionListener actionListener;
    private final IUserInfoRepository userInfoRepository;
    private final IApiRepository apiRepository;

    private String token;

    private boolean needSetEmail = false;

    @Inject
    ChatSdkInteractor(@NonNull Context context,
                      @NonNull UsedeskChatConfiguration configuration,
                      @NonNull UsedeskActionListener actionListener,
                      @NonNull IUserInfoRepository userInfoRepository,
                      @NonNull IApiRepository apiRepository) {
        this.context = context;
        this.userInfoRepository = userInfoRepository;
        this.apiRepository = apiRepository;

        this.configuration = configuration;
        this.actionListener = actionListener;
    }

    private <T> boolean equals(@Nullable T a, @Nullable T b) {
        return (a == null && b == null) || (a != null && a.equals(b));
    }

    @Override
    public void connect() throws UsedeskException {
        try {
            UsedeskChatConfiguration configuration = userInfoRepository.getConfiguration();
            if (this.configuration.getEmail().equals(configuration.getEmail())
                    && this.configuration.getCompanyId().equals(configuration.getCompanyId())) {
                token = userInfoRepository.getToken();
            }
            if (token != null && (!equals(this.configuration.getClientName(), configuration.getClientName())
                    || !equals(this.configuration.getClientPhoneNumber(), configuration.getClientPhoneNumber())
                    || !equals(this.configuration.getClientAdditionalId(), configuration.getClientAdditionalId()))) {
                needSetEmail = true;
            }
        } catch (UsedeskDataNotFoundException e) {
            e.printStackTrace();
        }

        userInfoRepository.setConfiguration(configuration);

        apiRepository.connect(configuration.getUrl(), actionListener, getOnMessageListener());
    }

    @Override
    public void disconnect() {
        apiRepository.disconnect();
    }

    @Override
    public void send(String textMessage) throws UsedeskException {
        if (textMessage == null || textMessage.isEmpty()) {
            return;
        }

        apiRepository.send(token, textMessage);
    }

    @Override
    public void send(UsedeskFileInfo usedeskFileInfo) throws UsedeskException {
        if (usedeskFileInfo == null) {
            return;
        }

        apiRepository.send(token, usedeskFileInfo);
    }

    @Override
    public void send(List<UsedeskFileInfo> usedeskFileInfoList) throws UsedeskException {
        if (usedeskFileInfoList == null) {
            return;
        }

        for (UsedeskFileInfo usedeskFileInfo : usedeskFileInfoList) {
            send(usedeskFileInfo);
        }
    }

    @Override
    public void send(Feedback feedback) throws UsedeskException {
        if (feedback == null) {
            return;
        }

        apiRepository.send(token, feedback);
    }

    @Override
    public void send(OfflineForm offlineForm) throws UsedeskException {
        if (offlineForm == null) {
            return;
        }
        if (offlineForm.getCompanyId() == null) {
            offlineForm = new OfflineForm(configuration.getCompanyId(), offlineForm.getName(), offlineForm.getEmail(), offlineForm.getMessage());
        }
        apiRepository.send(configuration, offlineForm);
    }

    @Override
    public void send(MessageButtons.MessageButton messageButton) throws UsedeskException {
        if (messageButton == null) {
            return;
        }
        if (messageButton.getUrl().isEmpty()) {
            send(messageButton.getText());
        } else {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(messageButton.getUrl()));//TODO: сделать обработчик ссылок и перенести туда вызов
            browserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(browserIntent);
        }
    }

    @NonNull
    @Override
    public Completable connectRx() {
        return Completable.create(emitter -> {
            connect();
            emitter.onComplete();
        });
    }

    @NonNull
    @Override
    public Completable sendRx(String textMessage) {
        return Completable.create(emitter -> {
            send(textMessage);
            emitter.onComplete();
        });
    }

    @NonNull
    @Override
    public Completable sendRx(UsedeskFileInfo usedeskFileInfo) {
        return Completable.create(emitter -> {
            send(usedeskFileInfo);
            emitter.onComplete();
        });
    }

    @NonNull
    @Override
    public Completable sendRx(List<UsedeskFileInfo> usedeskFileInfoList) {
        return Completable.create(emitter -> {
            send(usedeskFileInfoList);
            emitter.onComplete();
        });
    }

    @NonNull
    @Override
    public Completable sendRx(Feedback feedback) {
        return Completable.create(emitter -> {
            send(feedback);
            emitter.onComplete();
        });
    }

    @NonNull
    @Override
    public Completable sendRx(OfflineForm offlineForm) {
        return Completable.create(emitter -> {
            send(offlineForm);
            emitter.onComplete();
        });
    }

    @NonNull
    @Override
    public Completable sendRx(MessageButtons.MessageButton messageButton) {
        return Completable.create(emitter -> {
            send(messageButton);
            emitter.onComplete();
        });
    }

    @NonNull
    @Override
    public Completable disconnectRx() {
        return Completable.create(emitter -> {
            disconnect();
            emitter.onComplete();
        });
    }

    private void sendUserEmail() {
        apiRepository.send(token, configuration.getEmail(), configuration.getClientName(),
                configuration.getClientPhoneNumber(), configuration.getClientAdditionalId());
    }

    private void parseNewMessageResponse(Message message) {
        if (message != null && message.getChat() != null) {
            boolean hasText = !TextUtils.isEmpty(message.getText());
            boolean hasFile = message.getUsedeskFile() != null;

            if (hasText || hasFile) {
                actionListener.onMessageReceived(message);
            }
        }
    }

    private void parseFeedbackResponse() {
        Message message = new Message(MessageType.SERVICE);
        actionListener.onServiceMessageReceived(message);
    }

    private void parseInitResponse(String token, Setup setup) {
        this.token = token;
        userInfoRepository.setToken(token);

        actionListener.onConnected();

        if (setup != null) {
            if (setup.isWaitingEmail()) {
                needSetEmail = true;
            }

            if (setup.getMessages() != null && !setup.getMessages().isEmpty()) {
                actionListener.onMessagesReceived(setup.getMessages());
            }

            if (true/*setup.isNoOperators()*/) {//TODO: debug
                actionListener.onOfflineFormExpected();
            }
        } else {
            needSetEmail = true;
        }

        if (needSetEmail) {
            sendUserEmail();
        }
    }

    private OnMessageListener getOnMessageListener() {
        return new OnMessageListener() {
            @Override
            public void onNew(Message message) {
                parseNewMessageResponse(message);
            }

            @Override
            public void onFeedback() {
                parseFeedbackResponse();
            }

            @Override
            public void onInit(String token, Setup setup) {
                parseInitResponse(token, setup);
            }

            @Override
            public void onInitChat() {
                apiRepository.init(configuration, token);
            }

            @Override
            public void onTokenError() {
                userInfoRepository.setToken(null);
                token = null;

                apiRepository.init(configuration, token);
            }
        };
    }
}