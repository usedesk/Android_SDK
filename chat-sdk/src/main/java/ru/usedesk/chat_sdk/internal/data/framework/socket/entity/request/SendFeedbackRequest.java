package ru.usedesk.chat_sdk.internal.data.framework.socket.entity.request;

import com.google.gson.annotations.SerializedName;

import ru.usedesk.chat_sdk.external.entity.UsedeskFeedback;

public class SendFeedbackRequest extends BaseRequest {
    private static final String TYPE = "@@server/chat/CALLBACK";

    private static final String KEY_DATA = "data";
    private static final String VALUE_FEEDBACK_ACTION = "action";
    private final Payload payload;

    public SendFeedbackRequest(String token, UsedeskFeedback feedback) {
        super(TYPE, token);
        payload = new Payload(feedback);
    }

    private class Payload {

        private final String type;

        @SerializedName(KEY_DATA)
        private final UsedeskFeedback feedback;

        Payload(UsedeskFeedback feedback) {
            type = VALUE_FEEDBACK_ACTION;
            this.feedback = feedback;
        }
    }
}