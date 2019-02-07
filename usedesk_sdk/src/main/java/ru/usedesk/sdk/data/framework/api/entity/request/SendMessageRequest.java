package ru.usedesk.sdk.data.framework.api.entity.request;

public class SendMessageRequest extends BaseRequest {

    public static final String TYPE = "@@server/chat/SEND_MESSAGE";

    private RequestMessage message;

    public SendMessageRequest(String token, RequestMessage requestMessage) {
        super(TYPE, token);
        this.message = requestMessage;
    }

    public RequestMessage getRequestMessage() {
        return message;
    }
}