package ru.usedesk.sdk.internal.domain.interactor;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import ru.usedesk.sdk.R;
import ru.usedesk.sdk.external.entity.chat.Feedback;
import ru.usedesk.sdk.external.entity.chat.Message;
import ru.usedesk.sdk.external.entity.chat.MessageButtons;
import ru.usedesk.sdk.external.entity.chat.MessageType;
import ru.usedesk.sdk.external.entity.chat.OfflineForm;
import ru.usedesk.sdk.external.entity.chat.UsedeskActionListener;
import ru.usedesk.sdk.external.entity.chat.UsedeskConfiguration;
import ru.usedesk.sdk.external.entity.chat.UsedeskFile;
import ru.usedesk.sdk.external.entity.exceptions.DataNotFoundException;
import ru.usedesk.sdk.external.entity.exceptions.UsedeskSocketException;
import ru.usedesk.sdk.internal.data.framework.api.standard.entity.response.Setup;
import ru.usedesk.sdk.internal.domain.entity.chat.OnMessageListener;
import ru.usedesk.sdk.internal.domain.repositories.chat.IApiRepository;
import ru.usedesk.sdk.internal.domain.repositories.chat.IUserInfoRepository;

public class ChatInteractor {

    private Context context;
    private UsedeskConfiguration usedeskConfiguration;
    private UsedeskActionListener usedeskActionListener;
    private String token;
    private Thread thread;

    private IUserInfoRepository userInfoRepository;
    private IApiRepository apiRepository;

    private boolean needSetEmail = false;

    @Inject
    ChatInteractor(@NonNull Context context,
                   @NonNull IUserInfoRepository userInfoRepository,
                   @NonNull IApiRepository apiRepository) {
        this.context = context;
        this.userInfoRepository = userInfoRepository;
        this.apiRepository = apiRepository;
    }

    public void disconnect() {
        apiRepository.disconnect();

        if (thread != null) {
            thread.interrupt();
            thread = null;
        }
    }

    public void sendUserMessage(String text, UsedeskFile usedeskFile) {
        if (!apiRepository.isConnected()) {
            usedeskActionListener.onError(R.string.message_disconnected);
            usedeskActionListener.onException(new UsedeskSocketException(UsedeskSocketException.Error.DISCONNECTED));
            return;
        }

        sendMessage(text, usedeskFile);
    }

    public void sendUserMessage(String text, List<UsedeskFile> usedeskFiles) {
        if (usedeskFiles == null || usedeskFiles.isEmpty()) {
            return;
        }

        // checking for TEXT and ONE FILE
        if (usedeskFiles.size() == 1) {
            UsedeskFile usedeskFile = usedeskFiles.get(0);
            sendUserMessage(text, usedeskFile);
        } else {
            // first - message with TEXT
            sendUserTextMessage(text);

            // than - FILES
            for (UsedeskFile usedeskFile : usedeskFiles) {
                sendUserFileMessage(usedeskFile);
            }
        }
    }

    public void sendUserTextMessage(String text) {
        if (!apiRepository.isConnected()) {
            usedeskActionListener.onError(R.string.message_disconnected);
            usedeskActionListener.onException(new UsedeskSocketException(UsedeskSocketException.Error.DISCONNECTED));
            return;
        }

        sendMessage(text, null);
    }

    public void sendUserFileMessage(UsedeskFile usedeskFile) {
        if (!apiRepository.isConnected()) {
            usedeskActionListener.onError(R.string.message_disconnected);
            usedeskActionListener.onException(new UsedeskSocketException(UsedeskSocketException.Error.DISCONNECTED));
            return;
        }

        sendMessage(null, usedeskFile);
    }

    public void sendFeedbackMessage(Feedback feedback) {
        if (!apiRepository.isConnected()) {
            usedeskActionListener.onError(R.string.message_disconnected);
            usedeskActionListener.onException(new UsedeskSocketException(UsedeskSocketException.Error.DISCONNECTED));
            return;
        }

        apiRepository.sendFeedbackMessage(token, feedback);
    }

    public void sendOfflineForm(final OfflineForm offlineForm) {
        thread = new Thread(() -> postOfflineUrl(offlineForm));
        thread.start();
    }

    private void postOfflineUrl(final OfflineForm offlineForm) {
        boolean success = apiRepository.post(usedeskConfiguration, offlineForm);
        if (success) {
            usedeskActionListener.onServiceMessageReceived(new Message(MessageType.SERVICE,
                    context.getString(R.string.message_offline_form_sent)));
        }
    }

    public UsedeskConfiguration getUsedeskConfiguration() {
        return usedeskConfiguration;
    }

    private void initChat() {
        apiRepository.initChat(token, usedeskConfiguration);
    }

    private void sendMessage(String text, UsedeskFile usedeskFile) {
        apiRepository.sendMessageRequest(token, text, usedeskFile);
    }

    private void setUserEmail() {
        apiRepository.sendUserEmail(token, usedeskConfiguration.getEmail(),
                usedeskConfiguration.getClientName(),
                usedeskConfiguration.getClientPhoneNumber(),
                usedeskConfiguration.getClientAdditionalId());
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
        message.setText(context.getString(R.string.message_feedback_sent));
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
            apiRepository.setSocket(usedeskConfiguration.getUrl());
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

    private void onOfflineFormDialog() {
        Message message = new Message(MessageType.SERVICE);
        message.setText(context.getString(R.string.message_no_operators));
        usedeskActionListener.onServiceMessageReceived(message);
        usedeskActionListener.onOfflineFormExpected();
    }

    public void init(UsedeskConfiguration usedeskConfiguration,
                     UsedeskActionListener usedeskActionListener) {
        this.usedeskConfiguration = usedeskConfiguration;
        this.usedeskActionListener = usedeskActionListener;

        apiRepository.setActionListener(usedeskActionListener);

        try {
            UsedeskConfiguration configuration = userInfoRepository.getConfiguration();
            if (configuration.getEmail().equals(usedeskConfiguration.getEmail())
                    && configuration.getCompanyId().equals(usedeskConfiguration.getCompanyId())) {
                token = userInfoRepository.getToken();
            }
            if (token != null
                    && (!configuration.getClientName().equals(usedeskConfiguration.getClientName())
                    || !configuration.getClientPhoneNumber().equals(usedeskConfiguration.getClientPhoneNumber())
                    || !configuration.getClientAdditionalId().equals(usedeskConfiguration.getClientAdditionalId()))) {
                needSetEmail = true;
            }
        } catch (DataNotFoundException e) {
            e.printStackTrace();
        }

        userInfoRepository.setConfiguration(usedeskConfiguration);

        setSocket();
        connect();
    }

    public void onClickButtonWidget(@NonNull MessageButtons.MessageButton messageButton) {
        if (messageButton.getUrl().isEmpty()) {
            sendMessage(messageButton.getText(), null);
        } else {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(messageButton.getUrl()));
            browserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(browserIntent);
        }
    }
}