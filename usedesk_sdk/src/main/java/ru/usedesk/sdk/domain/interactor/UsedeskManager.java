package ru.usedesk.sdk.domain.interactor;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import org.json.JSONException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.inject.Inject;

import ru.usedesk.sdk.R;
import ru.usedesk.sdk.domain.boundaries.IApiRepository;
import ru.usedesk.sdk.domain.boundaries.IUserInfoRepository;
import ru.usedesk.sdk.domain.entity.Feedback;
import ru.usedesk.sdk.domain.entity.Message;
import ru.usedesk.sdk.domain.entity.MessageType;
import ru.usedesk.sdk.domain.entity.OfflineForm;
import ru.usedesk.sdk.domain.entity.OnMessageListener;
import ru.usedesk.sdk.domain.entity.Setup;
import ru.usedesk.sdk.domain.entity.UsedeskActionListener;
import ru.usedesk.sdk.domain.entity.UsedeskConfiguration;
import ru.usedesk.sdk.domain.entity.UsedeskFile;
import ru.usedesk.sdk.domain.entity.exceptions.ApiException;
import ru.usedesk.sdk.domain.entity.exceptions.DataNotFoundException;

import static ru.usedesk.sdk.domain.entity.Constants.OFFLINE_FORM_PATH;
import static ru.usedesk.sdk.utils.LogUtils.LOGD;
import static ru.usedesk.sdk.utils.LogUtils.LOGE;

public class UsedeskManager {

    private static final String TAG = UsedeskManager.class.getSimpleName();

    private Context context;
    private UsedeskConfiguration usedeskConfiguration;
    private UsedeskActionListener usedeskActionListener;
    private String token;
    private Thread thread;

    private IUserInfoRepository userInfoRepository;
    private IApiRepository apiRepository;

    @Inject
    public UsedeskManager(@NonNull Context context,
                          @NonNull UsedeskConfiguration usedeskConfiguration,
                          @NonNull UsedeskActionListener usedeskActionListener,
                          @NonNull IUserInfoRepository userInfoRepository,
                          @NonNull IApiRepository apiRepository) {
        this.context = context;
        this.usedeskConfiguration = usedeskConfiguration;
        this.usedeskActionListener = usedeskActionListener;
        this.userInfoRepository = userInfoRepository;
        this.apiRepository = apiRepository;

        apiRepository.setActionListener(usedeskActionListener);

        try {
            UsedeskConfiguration configuration = userInfoRepository.getConfiguration();
            if (configuration.equals(usedeskConfiguration)) {
                token = userInfoRepository.getToken();
            }
        } catch (DataNotFoundException e) {
            LOGD(TAG, e);
        }

        userInfoRepository.setConfiguration(usedeskConfiguration);

        setSocket();
        connect();
    }

    public void updateUsedeskConfiguration(UsedeskConfiguration usedeskConfiguration) {
        this.usedeskConfiguration = usedeskConfiguration;
    }

    public void updateUsedeskActionListener(UsedeskActionListener usedeskActionListener) {
        this.usedeskActionListener = usedeskActionListener;
    }

    public void reConnect() {
        disconnect();

        setSocket();
        connect();
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
            return;
        }

        sendMessage(text, null);
    }

    public void sendUserFileMessage(UsedeskFile usedeskFile) {
        if (!apiRepository.isConnected()) {
            usedeskActionListener.onError(R.string.message_disconnected);
            return;
        }

        sendMessage(null, usedeskFile);
    }

    public void sendFeedbackMessage(Feedback feedback) {
        if (!apiRepository.isConnected()) {
            usedeskActionListener.onError(R.string.message_disconnected);
            return;
        }

        apiRepository.sendFeedbackMessage(token, feedback);
    }

    public void sendOfflineForm(final OfflineForm offlineForm) {
        if (!apiRepository.isConnected()) {
            usedeskActionListener.onError(R.string.message_disconnected);
            return;
        }

        thread = new Thread(() -> postOfflineUrl(offlineForm));
        thread.start();
    }

    private void postOfflineUrl(final OfflineForm offlineForm) {
        try {
            String postUrl;
            try {
                UsedeskConfiguration configuration = userInfoRepository.getConfiguration();
                URL url = new URL(configuration.getUrl());
                postUrl = String.format(OFFLINE_FORM_PATH, url.getHost());
            } catch (MalformedURLException | DataNotFoundException e) {
                LOGE(TAG, e);
                return;
            }

            boolean success = apiRepository.post(postUrl, offlineForm.toJSONString());
            if (success) {
                Message message = new Message(MessageType.SERVICE);
                message.setText(context.getString(R.string.message_offline_form_sent));
                usedeskActionListener.onServiceMessageReceived(message);
            }
        } catch (JSONException e) {
            LOGE(TAG, e);
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
        apiRepository.sendUserEmail(token, usedeskConfiguration.getEmail());
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
                Message message = new Message(MessageType.SERVICE);
                message.setText(context.getString(R.string.message_no_operators));
                usedeskActionListener.onServiceMessageReceived(message);
                usedeskActionListener.onOfflineFormExpected();
            }
        } else {
            setUserEmail();
        }
    }

    private void setSocket() {
        try {
            apiRepository.setSocket(usedeskConfiguration.getUrl());
        } catch (ApiException e) {
            LOGE(TAG, e);

            usedeskActionListener.onError(e);
        }
    }

    private void connect() {
        apiRepository.connect(getOnMessageListener());
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
}