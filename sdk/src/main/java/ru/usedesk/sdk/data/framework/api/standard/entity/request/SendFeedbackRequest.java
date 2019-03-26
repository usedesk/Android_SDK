package ru.usedesk.sdk.data.framework.api.standard.entity.request;

import com.google.gson.annotations.SerializedName;

import ru.usedesk.sdk.domain.entity.chat.Constants;
import ru.usedesk.sdk.domain.entity.chat.Feedback;

public class SendFeedbackRequest extends BaseRequest {

    public static final String TYPE = "@@server/chat/CALLBACK";

    private Payload payload;

    public SendFeedbackRequest(String token, Feedback feedback) {
        super(TYPE, token);
        payload = new Payload(feedback);
    }

    private class Payload {

        private String type;

        @SerializedName(Constants.KEY_DATA)
        private Feedback feedback;

        public Payload(Feedback feedback) {
            type = Constants.VALUE_FEEDBACK_ACTION;
            this.feedback = feedback;
        }
    }
}