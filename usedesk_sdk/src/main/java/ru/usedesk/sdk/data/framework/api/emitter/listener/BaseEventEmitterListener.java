package ru.usedesk.sdk.data.framework.api.emitter.listener;

import java.net.HttpURLConnection;

import ru.usedesk.sdk.data.framework.ResponseProcessorImpl;
import ru.usedesk.sdk.data.framework.entity.response.BaseResponse;
import ru.usedesk.sdk.data.framework.entity.response.ErrorResponse;
import ru.usedesk.sdk.data.framework.entity.response.InitChatResponse;
import ru.usedesk.sdk.data.framework.entity.response.NewMessageResponse;
import ru.usedesk.sdk.data.framework.entity.response.SendFeedbackResponse;
import ru.usedesk.sdk.data.framework.entity.response.SetEmailResponse;
import ru.usedesk.sdk.data.repository.ResponseProcessor;
import ru.usedesk.sdk.domain.entity.Constants;
import ru.usedesk.sdk.domain.entity.OnMessageListener;
import ru.usedesk.sdk.domain.entity.UsedeskActionListener;

import static ru.usedesk.sdk.utils.LogUtils.LOGD;

public class BaseEventEmitterListener extends EmitterListener {
    private static final String TAG = BaseEventEmitterListener.class.getSimpleName();

    private ResponseProcessor<BaseResponse> responseProcessor = new ResponseProcessorImpl();

    public BaseEventEmitterListener(UsedeskActionListener actionListener,
                                    OnMessageListener onMessageListener) {
        super(actionListener, onMessageListener, Constants.EVENT_SERVER_ACTION);
    }

    @Override
    public void call(Object... args) {
        String rawResponse = args[0].toString();

        LOGD(TAG, "BaseEventEmitterListener.rawResponse = " + rawResponse);

        BaseResponse response = responseProcessor.process(rawResponse);

        if (response != null) {
            switch (response.getType()) {
                case ErrorResponse.TYPE:
                    ErrorResponse errorResponse = (ErrorResponse) response;
                    if (HttpURLConnection.HTTP_FORBIDDEN == errorResponse.getCode()) {
                        getOnMessageListener().onTokenError();
                    }
                    break;
                case InitChatResponse.TYPE:
                    InitChatResponse initChatResponse = (InitChatResponse) response;
                    getOnMessageListener().onInit(initChatResponse.getToken(),
                            initChatResponse.getSetup());
                    break;
                case SetEmailResponse.TYPE:
                    break;
                case NewMessageResponse.TYPE:
                    NewMessageResponse newMessageResponse = (NewMessageResponse) response;
                    getOnMessageListener().onNew(newMessageResponse.getMessage());
                    break;
                case SendFeedbackResponse.TYPE:
                    getOnMessageListener().onFeedback();
                    break;
            }
        }
    }
}
