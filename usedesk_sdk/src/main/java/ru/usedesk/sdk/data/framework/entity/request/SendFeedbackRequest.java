package ru.usedesk.sdk.data.framework.entity.request;

import com.google.gson.annotations.SerializedName;

import ru.usedesk.sdk.domain.entity.Feedback;

import static ru.usedesk.sdk.domain.entity.Constants.KEY_DATA;
import static ru.usedesk.sdk.domain.entity.Constants.VALUE_FEEDBACK_ACTION;

public class SendFeedbackRequest extends BaseRequest {

    public static final String TYPE = "@@server/chat/CALLBACK";

    private Payload payload;

    public SendFeedbackRequest(Feedback feedback) {
        super(TYPE);
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