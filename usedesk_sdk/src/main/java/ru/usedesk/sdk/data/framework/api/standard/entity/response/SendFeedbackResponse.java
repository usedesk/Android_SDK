package ru.usedesk.sdk.data.framework.api.standard.entity.response;

public class SendFeedbackResponse extends BaseResponse {

    public static final String TYPE = "@@chat/current/CALLBACK_ANSWER";

    private Answer answer;

    public SendFeedbackResponse() {
        super(TYPE);
    }

    public Answer getAnswer() {
        return answer;
    }

    private class Answer {

        private boolean status;

        private boolean isStatus() {
            return status;
        }
    }
}