package ru.usedesk.sdk.domain.interactor;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import org.json.JSONException;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import io.socket.emitter.Emitter;
import ru.usedesk.sdk.R;
import ru.usedesk.sdk.data.framework.ResponseProcessorImpl;
import ru.usedesk.sdk.data.framework.api.HttpApi;
import ru.usedesk.sdk.data.framework.entity.request.BaseRequest;
import ru.usedesk.sdk.data.framework.entity.request.InitChatRequest;
import ru.usedesk.sdk.data.framework.entity.request.SendFeedbackRequest;
import ru.usedesk.sdk.data.framework.entity.request.SendMessageRequest;
import ru.usedesk.sdk.data.framework.entity.request.SetEmailRequest;
import ru.usedesk.sdk.data.framework.entity.response.BaseResponse;
import ru.usedesk.sdk.data.framework.entity.response.ErrorResponse;
import ru.usedesk.sdk.data.framework.entity.response.InitChatResponse;
import ru.usedesk.sdk.data.framework.entity.response.NewMessageResponse;
import ru.usedesk.sdk.data.framework.entity.response.SendFeedbackResponse;
import ru.usedesk.sdk.data.framework.entity.response.SetEmailResponse;
import ru.usedesk.sdk.domain.boundaries.IUserInfoRepository;
import ru.usedesk.sdk.domain.entity.Feedback;
import ru.usedesk.sdk.domain.entity.Message;
import ru.usedesk.sdk.domain.entity.MessageType;
import ru.usedesk.sdk.domain.entity.OfflineForm;
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

    private BaseEventEmitterListener baseEventEmitterListener;
    private ConnectEmitterListener connectEmitterListener;
    private ConnectErrorEmitterListener connectErrorEmitterListener;
    private DisconnectEmitterListener disconnectEmitterListener;

    private IUserInfoRepository userInfoRepository;
    private HttpApi httpApi;
    private SocketApi socketApi;

    @Inject
    public UsedeskManager(@NonNull Context context,
                          @NonNull UsedeskConfiguration usedeskConfiguration,
                          @NonNull UsedeskActionListener usedeskActionListener,
                          @NonNull IUserInfoRepository userInfoRepository) {
        this.context = context;
        this.usedeskConfiguration = usedeskConfiguration;
        this.usedeskActionListener = usedeskActionListener;
        this.userInfoRepository = userInfoRepository;

        try {
            UsedeskConfiguration configuration = userInfoRepository.getConfiguration();
            if (configuration.equals(usedeskConfiguration)) {
                token = userInfoRepository.getToken();
            }
        } catch (DataNotFoundException e) {
            LOGD(TAG, e);
        }

        userInfoRepository.setConfiguration(usedeskConfiguration);

        baseEventEmitterListener = new BaseEventEmitterListener();
        connectEmitterListener = new ConnectEmitterListener();
        connectErrorEmitterListener = new ConnectErrorEmitterListener();
        disconnectEmitterListener = new DisconnectEmitterListener();

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
        socketApi.disconnect();

        if (thread != null) {
            thread.interrupt();
            thread = null;
        }
    }

    public void sendUserMessage(String text, UsedeskFile usedeskFile) {
        if (!socketApi.isConnected()) {
            usedeskActionListener.onError(R.string.message_disconnected);
            return;
        }

        SendMessageRequest.Message sendMessage = new SendMessageRequest.Message();
        sendMessage.setText(text);
        sendMessage.setUsedeskFile(usedeskFile);

        sendMessage(sendMessage);
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
        if (!socketApi.isConnected()) {
            usedeskActionListener.onError(R.string.message_disconnected);
            return;
        }

        SendMessageRequest.Message sendMessage = new SendMessageRequest.Message();
        sendMessage.setText(text);

        sendMessage(sendMessage);
    }

    public void sendUserFileMessage(UsedeskFile usedeskFile) {
        if (!socketApi.isConnected()) {
            usedeskActionListener.onError(R.string.message_disconnected);
            return;
        }

        SendMessageRequest.Message sendMessage = new SendMessageRequest.Message();
        sendMessage.setUsedeskFile(usedeskFile);

        sendMessage(sendMessage);
    }

    public void sendFeedbackMessage(Feedback feedback) {
        if (!socketApi.isConnected()) {
            usedeskActionListener.onError(R.string.message_disconnected);
            return;
        }

        SendFeedbackRequest feedbackRequest = new SendFeedbackRequest(feedback);
        feedbackRequest.setToken(token);
        emitAction(feedbackRequest);
    }

    public void sendOfflineForm(final OfflineForm offlineForm) {
        if (!socketApi.isConnected()) {
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

            boolean success = httpApi.post(postUrl, offlineForm.toJSONString());
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
        InitChatRequest initChatRequest = new InitChatRequest();
        initChatRequest.setToken(token);
        initChatRequest.setCompanyId(usedeskConfiguration.getCompanyId());
        initChatRequest.setUrl(usedeskConfiguration.getUrl());

        emitAction(initChatRequest);
    }

    private void sendMessage(SendMessageRequest.Message sendMessage) {
        SendMessageRequest sendMessageRequest = new SendMessageRequest();
        sendMessageRequest.setToken(token);
        sendMessageRequest.setMessage(sendMessage);

        emitAction(sendMessageRequest);
    }

    private void setUserEmail() {
        SetEmailRequest setEmailRequest = new SetEmailRequest();
        setEmailRequest.setToken(token);
        setEmailRequest.setEmail(usedeskConfiguration.getEmail());

        emitAction(setEmailRequest);
    }

    private void parseNewMessageResponse(NewMessageResponse newMessageResponse) {
        if (newMessageResponse.getMessage() != null && newMessageResponse.getMessage().getChat() != null) {
            boolean hasText = !TextUtils.isEmpty(newMessageResponse.getMessage().getText());
            boolean hasFile = newMessageResponse.getMessage().getUsedeskFile() != null;

            if (hasText || hasFile) {
                usedeskActionListener.onMessageReceived(newMessageResponse.getMessage());
            }
        }
    }

    private void parseFeedbackResponse(SendFeedbackResponse response) {
        Message message = new Message(MessageType.SERVICE);
        message.setText(context.getString(R.string.message_feedback_sent));
        usedeskActionListener.onServiceMessageReceived(message);
    }

    private void parseErrorResponse(ErrorResponse response) {
        if (HttpURLConnection.HTTP_FORBIDDEN == response.getCode()) {
            userInfoRepository.setToken(null);
            token = null;

            initChat();
        }
    }

    private void parseInitResponse(InitChatResponse response) {
        token = response.getToken();
        userInfoRepository.setToken(token);

        usedeskActionListener.onConnected();

        Setup setup = response.getSetup();
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
            socketApi.setSocket(usedeskConfiguration.getUrl());
        } catch (ApiException e) {
            LOGE(TAG, e);

            usedeskActionListener.onError(e);
        }
    }

    private void connect() {
        socketApi.connect();
    }

    private void emitAction(BaseRequest baseRequest) {
        try {
            socketApi.emitterAction(baseRequest);
        } catch (ApiException e) {
            LOGE(TAG, e);

            usedeskActionListener.onError(e);
        }
    }

    private class ConnectEmitterListener implements Emitter.Listener {

        @Override
        public void call(Object... args) {
            LOGD(TAG, "ConnectEmitterListener.args = " + Arrays.toString(args));
            initChat();
        }
    }

    private class ConnectErrorEmitterListener implements Emitter.Listener {

        @Override
        public void call(Object... args) {
            LOGE(TAG, "Error connecting: + " + Arrays.toString(args));

            usedeskActionListener.onError(R.string.message_connecting_error);
        }
    }

    private class DisconnectEmitterListener implements Emitter.Listener {

        @Override
        public void call(Object... args) {
            LOGE(TAG, "Disconnected.");

            usedeskActionListener.onDisconnected();
        }
    }

    private class BaseEventEmitterListener implements Emitter.Listener {

        private ResponseProcessorImpl responseProcessor;

        BaseEventEmitterListener() {
            responseProcessor = new ResponseProcessorImpl();
        }

        @Override
        public void call(Object... args) {
            String rawResponse = args[0].toString();

            LOGD(TAG, "BaseEventEmitterListener.rawResponse = " + rawResponse);

            BaseResponse response = responseProcessor.process(rawResponse);

            if (response != null) {
                switch (response.getType()) {
                    case ErrorResponse.TYPE:
                        parseErrorResponse((ErrorResponse) response);
                        break;
                    case InitChatResponse.TYPE:
                        parseInitResponse((InitChatResponse) response);
                        break;
                    case SetEmailResponse.TYPE:
                        break;
                    case NewMessageResponse.TYPE:
                        parseNewMessageResponse((NewMessageResponse) response);
                        break;
                    case SendFeedbackResponse.TYPE:
                        parseFeedbackResponse((SendFeedbackResponse) response);
                        break;
                }
            }
        }
    }
}