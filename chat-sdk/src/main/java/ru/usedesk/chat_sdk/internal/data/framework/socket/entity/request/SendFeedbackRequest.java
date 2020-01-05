package ru.usedesk.chat_sdk.internal.data.framework.socket.entity.request;

import com.google.gson.annotations.SerializedName;

import ru.usedesk.sdk.external.entity.chat.Feedback;

public class SendFeedbackRequest extends BaseRequest {
    public static final String TYPE = "@@server/chat/CALLBACK";
    private static final String KEY_DATA = "data";
    private static final String VALUE_FEEDBACK_ACTION = "action";
    private Payload payload;

    public SendFeedbackRequest(String token, Feedback feedback) {
        super(TYPE, token);
        payload = new Payload(feedback);
    }

    private class Payload {

        private String type;

        @SerializedName(KEY_DATA)
        private Feedback feedback;

        public Payload(Feedback feedback) {
            type = VALUE_FEEDBACK_ACTION;
            this.feedback = feedback;
        }
    }
}