package ru.usedesk.sdk;

import android.content.Context;
import android.text.TextUtils;

import org.json.JSONException;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import ru.usedesk.sdk.models.BaseRequest;
import ru.usedesk.sdk.models.BaseResponse;
import ru.usedesk.sdk.models.ErrorResponse;
import ru.usedesk.sdk.models.Feedback;
import ru.usedesk.sdk.models.InitChatRequest;
import ru.usedesk.sdk.models.InitChatResponse;
import ru.usedesk.sdk.models.Message;
import ru.usedesk.sdk.models.MessageType;
import ru.usedesk.sdk.models.NewMessageResponse;
import ru.usedesk.sdk.models.OfflineForm;
import ru.usedesk.sdk.models.SendFeedbackRequest;
import ru.usedesk.sdk.models.SendFeedbackResponse;
import ru.usedesk.sdk.models.SendMessageRequest;
import ru.usedesk.sdk.models.SetEmailRequest;
import ru.usedesk.sdk.models.SetEmailResponse;
import ru.usedesk.sdk.models.Setup;
import ru.usedesk.sdk.models.UsedeskFile;
import ru.usedesk.sdk.utils.LogUtils;
import ru.usedesk.sdk.utils.SharedHelper;

import static ru.usedesk.sdk.Constants.OFFLINE_FORM_PATH;

class UsedeskManager {

    private static final String TAG = UsedeskManager.class.getSimpleName();

    private Context context;
    private SharedHelper sharedHelper;
    private UsedeskConfiguration usedeskConfiguration;
    private UsedeskActionListener usedeskActionListener;
    private Socket socket;
    private String token;
    private Thread thread;

    private BaseEventEmitterListener baseEventEmitterListener;
    private ConnectEmitterListener connectEmitterListener;
    private ConnectErrorEmitterListener connectErrorEmitterListener;
    private DisconnectEmitterListener disconnectEmitterListener;

    UsedeskManager(Context context,
            UsedeskConfiguration usedeskConfiguration,
            UsedeskActionListener usedeskActionListener) {
        this.context = context;
        this.sharedHelper = new SharedHelper(context);
        this.usedeskConfiguration = usedeskConfiguration;
        this.usedeskActionListener = usedeskActionListener;
        token = sharedHelper.changedEmail(usedeskConfiguration.getEmail()) ? null : sharedHelper.getToken();

        sharedHelper.saveUrl(usedeskConfiguration.getUrl());
        sharedHelper.saveEmail(usedeskConfiguration.getEmail());

        baseEventEmitterListener = new BaseEventEmitterListener();
        connectEmitterListener = new ConnectEmitterListener();
        connectErrorEmitterListener = new ConnectErrorEmitterListener();
        disconnectEmitterListener = new DisconnectEmitterListener();

        setSocket();
        connect();
    }

    void updateUsedeskConfiguration(UsedeskConfiguration usedeskConfiguration) {
        this.usedeskConfiguration = usedeskConfiguration;
    }

    void updateUsedeskActionListener(UsedeskActionListener usedeskActionListener) {
        this.usedeskActionListener = usedeskActionListener;
    }

    void reConnect() {
        disconnect();

        setSocket();
        connect();
    }

    void disconnect() {
        socket.off(Socket.EVENT_CONNECT, connectEmitterListener);
        socket.off(Socket.EVENT_DISCONNECT, disconnectEmitterListener);
        socket.off(Socket.EVENT_CONNECT_ERROR, connectErrorEmitterListener);
        socket.off(Socket.EVENT_CONNECT_TIMEOUT, connectErrorEmitterListener);
        socket.off(Constants.EVENT_SERVER_ACTION, baseEventEmitterListener);
        socket.disconnect();

        if (thread != null) {
            thread.interrupt();
            thread = null;
        }
    }

    void sendUserMessage(String text, UsedeskFile usedeskFile) {
        if (!socket.connected()) {
            usedeskActionListener.onError(R.string.message_disconnected);
            return;
        }

        SendMessageRequest.Message sendMessage = new SendMessageRequest.Message();
        sendMessage.setText(text);
        sendMessage.setUsedeskFile(usedeskFile);

        sendMessage(sendMessage);
    }

    void sendUserMessage(String text, List<UsedeskFile> usedeskFiles) {
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

    void sendUserTextMessage(String text) {
        if (!socket.connected()) {
            usedeskActionListener.onError(R.string.message_disconnected);
            return;
        }

        SendMessageRequest.Message sendMessage = new SendMessageRequest.Message();
        sendMessage.setText(text);

        sendMessage(sendMessage);
    }

    void sendUserFileMessage(UsedeskFile usedeskFile) {
        if (!socket.connected()) {
            usedeskActionListener.onError(R.string.message_disconnected);
            return;
        }

        SendMessageRequest.Message sendMessage = new SendMessageRequest.Message();
        sendMessage.setUsedeskFile(usedeskFile);

        sendMessage(sendMessage);
    }

    void sendFeedbackMessage(Feedback feedback) {
        if (!socket.connected()) {
            usedeskActionListener.onError(R.string.message_disconnected);
            return;
        }

        SendFeedbackRequest feedbackRequest = new SendFeedbackRequest(feedback);
        feedbackRequest.setToken(token);
        emitAction(feedbackRequest);
    }

    void sendOfflineForm(final OfflineForm offlineForm) {
        if (!socket.connected()) {
            usedeskActionListener.onError(R.string.message_disconnected);
            return;
        }

        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String postUrl;
                    try {
                        URL url = new URL(sharedHelper.getUrl());
                        postUrl = String.format(OFFLINE_FORM_PATH, url.getHost());
                    } catch (MalformedURLException e) {
                        LogUtils.LOGE(TAG, e);
                        return;
                    }

                    boolean success = CallAPI.post(postUrl, offlineForm.toJSONString());
                    if (success) {
                        Message message = new Message(MessageType.SERVICE);
                        message.setText(context.getString(R.string.message_offline_form_sent));
                        usedeskActionListener.onServiceMessageReceived(message);
                    }
                } catch (JSONException e) {
                    LogUtils.LOGE(TAG, e);
                }
            }
        });
        thread.start();
    }

    UsedeskConfiguration getUsedeskConfiguration() {
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
            sharedHelper.saveToken(null);
            token = null;

            initChat();
        }
    }

    private void parseInitResponse(InitChatResponse response) {
        token = response.getToken();
        sharedHelper.saveToken(token);

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
            socket = IO.socket(usedeskConfiguration.getUrl());
        } catch (URISyntaxException e) {
            LogUtils.LOGE(TAG, e);

            usedeskActionListener.onError(e);
        }
    }

    private void connect() {
        if (socket == null) {
            return;
        }

        socket.on(Socket.EVENT_CONNECT, connectEmitterListener);
        socket.on(Socket.EVENT_DISCONNECT, disconnectEmitterListener);
        socket.on(Socket.EVENT_CONNECT_ERROR, connectErrorEmitterListener);
        socket.on(Socket.EVENT_CONNECT_TIMEOUT, connectErrorEmitterListener);
        socket.on(Constants.EVENT_SERVER_ACTION, baseEventEmitterListener);

        socket.connect();
    }

    private void emitAction(BaseRequest baseRequest) {
        if (socket == null) {
            return;
        }

        try {
            LogUtils.LOGD(TAG, "emitAction(). request = " + baseRequest.toJSONObject());

            socket.emit(Constants.EVENT_SERVER_ACTION, baseRequest.toJSONObject());
        } catch (JSONException e) {
            LogUtils.LOGE(TAG, e);

            usedeskActionListener.onError(e);
        }
    }

    private class ConnectEmitterListener implements Emitter.Listener {

        @Override
        public void call(Object... args) {
            LogUtils.LOGD(TAG, "ConnectEmitterListener.args = " + Arrays.toString(args));
            initChat();
        }
    }

    private class ConnectErrorEmitterListener implements Emitter.Listener {

        @Override
        public void call(Object... args) {
            LogUtils.LOGE(TAG, "Error connecting: + " + Arrays.toString(args));

            usedeskActionListener.onError(R.string.message_connecting_error);
        }
    }

    private class DisconnectEmitterListener implements Emitter.Listener {

        @Override
        public void call(Object... args) {
            LogUtils.LOGE(TAG, "Disconnected.");

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

            LogUtils.LOGD(TAG, "BaseEventEmitterListener.rawResponse = " + rawResponse);

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