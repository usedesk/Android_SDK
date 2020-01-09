package ru.usedesk.chat_sdk.internal.domain;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.Map;

import javax.inject.Inject;

import ru.usedesk.chat_sdk.R;
import ru.usedesk.chat_sdk.external.IUsedeskChatSdk;
import ru.usedesk.chat_sdk.external.entity.Feedback;
import ru.usedesk.chat_sdk.external.entity.Message;
import ru.usedesk.chat_sdk.external.entity.MessageButtons;
import ru.usedesk.chat_sdk.external.entity.MessageType;
import ru.usedesk.chat_sdk.external.entity.OfflineForm;
import ru.usedesk.chat_sdk.external.entity.OnMessageListener;
import ru.usedesk.chat_sdk.external.entity.UsedeskActionListener;
import ru.usedesk.chat_sdk.external.entity.UsedeskChatConfiguration;
import ru.usedesk.chat_sdk.external.entity.UsedeskFile;
import ru.usedesk.chat_sdk.external.entity.UsedeskFileInfo;
import ru.usedesk.chat_sdk.internal.data.framework.socket.entity.response.Setup;
import ru.usedesk.chat_sdk.internal.data.repository.api.IApiRepository;
import ru.usedesk.chat_sdk.internal.data.repository.configuration.IUserInfoRepository;
import ru.usedesk.common_sdk.external.entity.exceptions.DataNotFoundException;
import ru.usedesk.common_sdk.external.entity.exceptions.UsedeskHttpException;
import ru.usedesk.common_sdk.external.entity.exceptions.UsedeskSocketException;

public class ChatSdk implements IUsedeskChatSdk {

    private Context context;
    private UsedeskChatConfiguration usedeskChatConfiguration;
    private UsedeskActionListener usedeskActionListener;
    private String token;

    private IUserInfoRepository userInfoRepository;
    private IApiRepository apiRepository;

    private boolean needSetEmail = false;

    @Inject
    ChatSdk(@NonNull Context context,
            @NonNull IUserInfoRepository userInfoRepository,
            @NonNull IApiRepository apiRepository) {
        this.context = context;
        this.userInfoRepository = userInfoRepository;
        this.apiRepository = apiRepository;
    }

    private <T> boolean equals(@Nullable T a, @Nullable T b) {
        return (a == null && b == null) || (a != null && a.equals(b));
    }

    @Override
    public void init(@NonNull UsedeskChatConfiguration usedeskChatConfiguration,
                     @NonNull UsedeskActionListener usedeskActionListener) {
        this.usedeskChatConfiguration = usedeskChatConfiguration;
        this.usedeskActionListener = usedeskActionListener;

        apiRepository.setActionListener(usedeskActionListener);

        try {
            UsedeskChatConfiguration configuration = userInfoRepository.getConfiguration();
            if (usedeskChatConfiguration.getEmail().equals(configuration.getEmail())
                    && usedeskChatConfiguration.getCompanyId().equals(configuration.getCompanyId())) {
                token = userInfoRepository.getToken();
            }
            if (token != null && (!equals(usedeskChatConfiguration.getClientName(), configuration.getClientName())
                    || !equals(usedeskChatConfiguration.getClientPhoneNumber(), configuration.getClientPhoneNumber())
                    || !equals(usedeskChatConfiguration.getClientAdditionalId(), configuration.getClientAdditionalId()))) {
                needSetEmail = true;
            }
        } catch (DataNotFoundException e) {
            e.printStackTrace();
        }

        userInfoRepository.setConfiguration(usedeskChatConfiguration);

        setSocket();
        connect();
    }

    @Override
    public void disconnect() {
        apiRepository.disconnect();
    }

    @Override
    public void sendUserFileMessage(@NonNull UsedeskFileInfo usedeskFileInfo) {
        //sendMessage(null, null);//TODO:
    }

    @Override
    public void sendUserTextMessage(String text) {
        if (text == null || text.isEmpty()) {
            return;
        }

        if (!apiRepository.isConnected()) {
            usedeskActionListener.onError(R.string.message_disconnected);
            usedeskActionListener.onException(new UsedeskSocketException(UsedeskSocketException.Error.DISCONNECTED));
            return;
        }

        sendMessage(text, null);
    }

    @Override
    public void sendFeedbackMessage(Feedback feedback) {
        if (feedback == null) {
            return;
        }
        if (!apiRepository.isConnected()) {
            usedeskActionListener.onError(R.string.message_disconnected);
            usedeskActionListener.onException(new UsedeskSocketException(UsedeskSocketException.Error.DISCONNECTED));
            return;
        }

        apiRepository.sendFeedbackMessage(token, feedback);
    }

    @Override
    public void sendOfflineForm(OfflineForm offlineForm) throws UsedeskHttpException {
        if (offlineForm == null) {
            return;
        }
        apiRepository.post(usedeskChatConfiguration, offlineForm);
    }

    @Override
    public void onClickButtonWidget(MessageButtons.MessageButton messageButton) {
        if (messageButton == null) {
            return;
        }
        if (messageButton.getUrl().isEmpty()) {
            sendMessage(messageButton.getText(), null);
        } else {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(messageButton.getUrl()));//TODO
            browserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(browserIntent);
        }
    }

    public UsedeskChatConfiguration getUsedeskChatConfiguration() {
        return usedeskChatConfiguration;
    }

    private void initChat() {
        apiRepository.initChat(token, usedeskChatConfiguration);
    }

    private void sendMessage(String text, UsedeskFile usedeskFile) {
        apiRepository.sendMessageRequest(token, text, usedeskFile);
    }

    private void setUserEmail() {
        apiRepository.sendUserEmail(token, usedeskChatConfiguration.getEmail(),
                usedeskChatConfiguration.getClientName(),
                usedeskChatConfiguration.getClientPhoneNumber(),
                usedeskChatConfiguration.getClientAdditionalId());
    }

    private void parseNewMessageResponse(Message message) {
        if (message != null && message.getChat() != null) {
            boolean hasText = !TextUtils.isEmpty(message.getText());
            boolean hasFile = message.getUsedeskFile() != null;

            if (hasText || hasFile) {
                usedeskActionListener.onMessageReceived(message);
            }
        }
    }

    private void parseFeedbackResponse() {
        Message message = new Message(MessageType.SERVICE);
        message.setText(context.getString(R.string.message_feedback_sent));//TODO
        usedeskActionListener.onServiceMessageReceived(message);
    }

    private void parseInitResponse(String token, Setup setup) {
        this.token = token;
        userInfoRepository.setToken(token);

        usedeskActionListener.onConnected();

        if (setup != null) {
            if (setup.isWaitingEmail()) {
                setUserEmail();
            }

            if (setup.getMessages() != null && !setup.getMessages().isEmpty()) {
                usedeskActionListener.onMessagesReceived(setup.getMessages());
            }

            if (setup.isNoOperators()) {
                onOfflineFormDialog();
            }
        } else {
            setUserEmail();
        }
    }

    private void setSocket() {
        try {
            apiRepository.setSocket(usedeskChatConfiguration.getUrl());
        } catch (UsedeskSocketException e) {
            usedeskActionListener.onError(e);
            usedeskActionListener.onException(e);
        }
    }

    private void connect() {
        apiRepository.connect(getOnMessageListener());
    }

    private OnMessageListener getOnMessageListener() {
        return new OnMessageListener() {
            @Override
            public void onNew(Message message) {
                try {
                    if (message != null && message.getPayloadAsObject() != null) {
                        Map map = (Map) message.getPayloadAsObject();

                        Boolean noOperators = (Boolean) map.get("noOperators");//TODO: выпилить отсюда и впилить по доке (когда сервер начнёт отдавать что нужно)

                        if (noOperators != null && noOperators) {
                            onOfflineFormDialog();
                            return;
                        }
                    }
                } catch (ClassCastException e) {
                    //nothing
                }
                parseNewMessageResponse(message);
            }

            @Override
            public void onFeedback() {
                parseFeedbackResponse();
            }

            @Override
            public void onInit(String token, Setup setup) {
                parseInitResponse(token, setup);

                if (needSetEmail) {
                    setUserEmail();
                }
            }

            @Override
            public void onInitChat() {
                initChat();
            }

            @Override
            public void onTokenError() {
                userInfoRepository.setToken(null);
                token = null;

                initChat();
            }
        };
    }

    private void onOfflineFormDialog() {//TODO
        Message message = new Message(MessageType.SERVICE);
        message.setText(context.getString(R.string.message_no_operators));
        usedeskActionListener.onServiceMessageReceived(message);
        usedeskActionListener.onOfflineFormExpected();
    }
}