package ru.usedesk.sdk.data.framework.entity.request;

import ru.usedesk.sdk.domain.entity.Feedback;

import static ru.usedesk.sdk.domain.entity.Constants.VALUE_FEEDBACK_ACTION;

public class SendFeedbackRequest extends BaseRequest {

    public static final String TYPE = "@@server/chat/CALLBACK";

    private Payload payload;

    public SendFeedbackRequest(String token, Feedback feedback) {
        super(TYPE, token);
        payload = new Payload(feedback);
    }

    private class Payload {

        private String type;

        private Feedback data;

        public Payload(Feedback feedback) {
            type = VALUE_FEEDBACK_ACTION;
            this.data = feedback;
        }
    }
}