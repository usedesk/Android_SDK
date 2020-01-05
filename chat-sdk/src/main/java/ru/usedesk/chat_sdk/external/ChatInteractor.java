package ru.usedesk.chat_sdk.external;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.disposables.Disposable;
import ru.usedesk.sdk.R;
import ru.usedesk.sdk.external.entity.chat.Feedback;
import ru.usedesk.sdk.external.entity.chat.Message;
import ru.usedesk.sdk.external.entity.chat.MessageButtons;
import ru.usedesk.sdk.external.entity.chat.MessageType;
import ru.usedesk.sdk.external.entity.chat.OfflineForm;
import ru.usedesk.sdk.external.entity.chat.UsedeskActionListener;
import ru.usedesk.sdk.external.entity.chat.UsedeskConfiguration;
import ru.usedesk.sdk.external.entity.chat.UsedeskFile;
import ru.usedesk.sdk.external.entity.chat.UsedeskFileInfo;
import ru.usedesk.sdk.external.entity.exceptions.DataNotFoundException;
import ru.usedesk.sdk.external.entity.exceptions.UsedeskException;
import ru.usedesk.sdk.external.entity.exceptions.UsedeskHttpException;
import ru.usedesk.sdk.external.entity.exceptions.UsedeskSocketException;
import ru.usedesk.sdk.internal.data.framework.api.standard.entity.response.Setup;
import ru.usedesk.sdk.internal.domain.entity.chat.OnMessageListener;

public class ChatInteractor {

    private Context context;
    private UsedeskConfiguration usedeskConfiguration;
    private UsedeskActionListener usedeskActionListener;
    private String token;

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

    private <T> boolean equals(@Nullable T a, @Nullable T b) {
        return (a == null && b == null) || (a != null && a.equals(b));
    }

    @Override
    public void init(@NonNull UsedeskConfiguration usedeskConfiguration,
                     @NonNull UsedeskActionListener usedeskActionListener) {
        this.usedeskConfiguration = usedeskConfiguration;
        this.usedeskActionListener = usedeskActionListener;

        apiRepository.setActionListener(usedeskActionListener);

        try {
            UsedeskConfiguration configuration = userInfoRepository.getConfiguration();
            if (usedeskConfiguration.getEmail().equals(configuration.getEmail())
                    && usedeskConfiguration.getCompanyId().equals(configuration.getCompanyId())) {
                token = userInfoRepository.getToken();
            }
            if (token != null && (!equals(usedeskConfiguration.getClientName(), configuration.getClientName())
                    || !equals(usedeskConfiguration.getClientPhoneNumber(), configuration.getClientPhoneNumber())
                    || !equals(usedeskConfiguration.getClientAdditionalId(), configuration.getClientAdditionalId()))) {
                needSetEmail = true;
            }
        } catch (DataNotFoundException e) {
            e.printStackTrace();
        }

        userInfoRepository.setConfiguration(usedeskConfiguration);

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
        apiRepository.post(usedeskConfiguration, offlineForm);
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
        message.setText(context.getString(R.string.message_no_operators));//TODO
        usedeskActionListener.onServiceMessageReceived(message);
        usedeskActionListener.onOfflineFormExpected();
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

    @Deprecated
    public void sendOfflineFormSync(final OfflineForm offlineForm) {
        Disposable d = Completable.create(emitter -> {
            sendOfflineForm(offlineForm);
            emitter.onComplete();
        }).subscribe(() -> {
            usedeskActionListener.onServiceMessageReceived(new Message(MessageType.SERVICE,
                    context.getString(R.string.message_offline_form_sent)));
        }, throwable -> {
            if (throwable instanceof UsedeskException) {
                usedeskActionListener.onException((UsedeskException) throwable);
            } else {
                throwable.printStackTrace();
            }
        });
    }

    @Deprecated
    public void sendUserFileMessage(UsedeskFile usedeskFile) {
        if (!apiRepository.isConnected()) {
            usedeskActionListener.onError(R.string.message_disconnected);
            usedeskActionListener.onException(new UsedeskSocketException(UsedeskSocketException.Error.DISCONNECTED));
            return;
        }

        sendMessage(null, usedeskFile);
    }

    @Deprecated
    public void sendUserMessage(String text, UsedeskFile usedeskFile) {
        if (!apiRepository.isConnected()) {
            usedeskActionListener.onError(R.string.message_disconnected);
            usedeskActionListener.onException(new UsedeskSocketException(UsedeskSocketException.Error.DISCONNECTED));
            return;
        }

        sendMessage(text, usedeskFile);
    }

    @Deprecated
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
}