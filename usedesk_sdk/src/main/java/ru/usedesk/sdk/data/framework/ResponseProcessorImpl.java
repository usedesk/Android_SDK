package ru.usedesk.sdk.data.framework;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import ru.usedesk.sdk.data.framework.entity.response.BaseResponse;
import ru.usedesk.sdk.data.framework.entity.response.ErrorResponse;
import ru.usedesk.sdk.data.framework.entity.response.InitChatResponse;
import ru.usedesk.sdk.data.framework.entity.response.NewMessageResponse;
import ru.usedesk.sdk.data.framework.entity.response.SendFeedbackResponse;
import ru.usedesk.sdk.data.framework.entity.response.SetEmailResponse;
import ru.usedesk.sdk.data.repository.ResponseProcessor;
import ru.usedesk.sdk.domain.entity.RuntimeTypeAdapterFactory;

public class ResponseProcessorImpl implements ResponseProcessor<BaseResponse> {

    private static final String KEY_TYPE = "type";

    @Override
    public BaseResponse process(String rawResponse) {
        TypeToken<BaseResponse> typeToken = new TypeToken<BaseResponse>() {
        };

        RuntimeTypeAdapterFactory<BaseResponse> typeFactory = RuntimeTypeAdapterFactory
                .of(BaseResponse.class, KEY_TYPE)
                .registerSubtype(ErrorResponse.class, ErrorResponse.TYPE)
                .registerSubtype(InitChatResponse.class, InitChatResponse.TYPE)
                .registerSubtype(SetEmailResponse.class, SetEmailResponse.TYPE)
                .registerSubtype(NewMessageResponse.class, NewMessageResponse.TYPE)
                .registerSubtype(SendFeedbackResponse.class, SendFeedbackResponse.TYPE);

        Gson gson = new GsonBuilder().registerTypeAdapterFactory(typeFactory).create();

        return gson.fromJson(rawResponse, typeToken.getType());
    }
}