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
import ru.usedesk.chat_sdk.external.IUsedeskChat;
import ru.usedesk.chat_sdk.external.entity.IUsedeskActionListener;
import ru.usedesk.chat_sdk.external.entity.UsedeskChatConfiguration;
import ru.usedesk.chat_sdk.external.entity.UsedeskFeedback;
import ru.usedesk.chat_sdk.external.entity.UsedeskFileInfo;
import ru.usedesk.chat_sdk.external.entity.UsedeskMessage;
import ru.usedesk.chat_sdk.external.entity.UsedeskMessageButton;
import ru.usedesk.chat_sdk.external.entity.UsedeskOfflineForm;
import ru.usedesk.chat_sdk.internal.data.framework.socket.entity.response.Setup;
import ru.usedesk.chat_sdk.internal.data.repository.api.IApiRepository;
import ru.usedesk.chat_sdk.internal.data.repository.configuration.IUserInfoRepository;
import ru.usedesk.chat_sdk.internal.domain.entity.OnMessageListener;
import ru.usedesk.common_sdk.external.entity.exceptions.UsedeskDataNotFoundException;
import ru.usedesk.common_sdk.external.entity.exceptions.UsedeskException;

public class ChatInteractor implements IUsedeskChat {

    private final Context context;
    private final UsedeskChatConfiguration configuration;
    private final IUsedeskActionListener actionListener;
    private final IUserInfoRepository userInfoRepository;
    private final IApiRepository apiRepository;

    private String token;
    private boolean needSetEmail = false;
    private String initClientMessage = null;

    @Inject
    ChatInteractor(@NonNull Context context,
                   @NonNull UsedeskChatConfiguration configuration,
                   @NonNull IUsedeskActionListener actionListener,
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

        apiRepository.connect(configuration.getSocketUrl(), actionListener, getOnMessageListener());
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

        apiRepository.send(configuration, token, usedeskFileInfo);
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
    public void send(UsedeskFeedback feedback) throws UsedeskException {
        if (feedback == null) {
            return;
        }

        apiRepository.send(token, feedback);
    }

    @Override
    public void send(UsedeskOfflineForm offlineForm) throws UsedeskException {
        if (offlineForm == null) {
            return;
        }
        if (offlineForm.getCompanyId() == null) {
            offlineForm = new UsedeskOfflineForm(configuration.getCompanyId(), offlineForm.getName(),
                    offlineForm.getEmail(), offlineForm.getMessage());
        }
        apiRepository.send(configuration, offlineForm);
    }

    @Override
    public void send(UsedeskMessageButton messageButton) throws UsedeskException {
        if (messageButton == null) {
            return;
        }
        if (messageButton.getUrl().isEmpty()) {
            send(messageButton.getText());
        } else {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(messageButton.getUrl()));
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
    public Completable sendRx(UsedeskFeedback feedback) {
        return Completable.create(emitter -> {
            send(feedback);
            emitter.onComplete();
        });
    }

    @NonNull
    @Override
    public Completable sendRx(UsedeskOfflineForm offlineForm) {
        return Completable.create(emitter -> {
            send(offlineForm);
            emitter.onComplete();
        });
    }

    @NonNull
    @Override
    public Completable sendRx(UsedeskMessageButton messageButton) {
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
        try {
            apiRepository.send(token, configuration.getEmail(), configuration.getClientName(),
                    configuration.getClientPhoneNumber(), configuration.getClientAdditionalId());
        } catch (UsedeskException e) {
            actionListener.onException(e);
        }
    }

    private void parseNewMessageResponse(UsedeskMessage message) {
        if (message != null && message.getChat() != null) {
            boolean hasText = !TextUtils.isEmpty(message.getText()) ||
                    !message.getMessageButtons().getMessageButtons().isEmpty();
            boolean hasFile = message.getFile() != null;

            if (hasText || hasFile) {
                actionListener.onMessageReceived(message);
            }
        }
    }

    private void parseInitResponse(String token, Setup setup) {
        this.token = token;
        userInfoRepository.setToken(token);

        actionListener.onConnected();

        if (setup != null) {
            if (setup.isWaitingEmail()) {
                needSetEmail = true;
            }

            actionListener.onMessagesReceived(setup.getMessages());

            if (setup.isNoOperators()) {
                actionListener.onOfflineFormExpected(configuration);
            }
        } else {
            needSetEmail = true;
        }

        try {
            String initClientMessage = userInfoRepository.getConfiguration().getInitClientMessage();
            if (!equals(initClientMessage, configuration.getInitClientMessage())) {
                this.initClientMessage = configuration.getInitClientMessage();
            }
        } catch (UsedeskException ignore) {
            this.initClientMessage = configuration.getInitClientMessage();
        }
        userInfoRepository.setConfiguration(configuration);

        if (needSetEmail) {
            sendUserEmail();
        } else {
            getOnMessageListener().onSetEmailSuccess();
        }
    }

    private OnMessageListener getOnMessageListener() {
        return new OnMessageListener() {
            @Override
            public void onNew(UsedeskMessage message) {
                parseNewMessageResponse(message);
            }

            @Override
            public void onFeedback() {
                actionListener.onFeedbackReceived();
            }

            @Override
            public void onInit(String token, Setup setup) {
                parseInitResponse(token, setup);
            }

            @Override
            public void onInitChat() {
                try {
                    apiRepository.init(configuration, token);
                } catch (UsedeskException e) {
                    actionListener.onException(e);
                }
            }

            @Override
            public void onTokenError() {
                userInfoRepository.setToken(null);
                token = null;

                try {
                    apiRepository.init(configuration, token);
                } catch (UsedeskException e) {
                    actionListener.onException(e);
                }
            }

            @Override
            public void onSetEmailSuccess() {
                if (initClientMessage != null && !initClientMessage.isEmpty()) {
                    try {
                        send(initClientMessage);
                        initClientMessage = "";
                    } catch (Exception e) {
                        //nothing
                    }
                }
            }
        };
    }
}