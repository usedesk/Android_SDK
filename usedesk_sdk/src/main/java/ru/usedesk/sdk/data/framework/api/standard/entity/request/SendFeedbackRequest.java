package ru.usedesk.sdk.data.framework.api.standard.entity.request;

import com.google.gson.annotations.SerializedName;

import ru.usedesk.sdk.domain.entity.chat.Feedback;

import static ru.usedesk.sdk.domain.entity.chat.Constants.KEY_DATA;
import static ru.usedesk.sdk.domain.entity.chat.Constants.VALUE_FEEDBACK_ACTION;

public class SendFeedbackRequest extends BaseRequest {

    public static final String TYPE = "@@server/chat/CALLBACK";

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