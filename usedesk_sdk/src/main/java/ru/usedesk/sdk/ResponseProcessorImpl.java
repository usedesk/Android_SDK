package ru.usedesk.sdk;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import ru.usedesk.sdk.models.BaseResponse;
import ru.usedesk.sdk.models.ErrorResponse;
import ru.usedesk.sdk.models.InitChatResponse;
import ru.usedesk.sdk.models.NewMessageResponse;
import ru.usedesk.sdk.models.SendFeedbackResponse;
import ru.usedesk.sdk.models.SetEmailResponse;

public class ResponseProcessorImpl implements ResponseProcessor<BaseResponse> {

    private static final String KEY_TYPE = "type";

    @Override
    public BaseResponse process(String rawResponse) {
        TypeToken<BaseResponse> typeToken = new TypeToken<BaseResponse>() {};

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