package ru.usedesk.sdk.data.framework.entity.request;

public class SendMessageRequest extends BaseRequest {

    public static final String TYPE = "@@server/chat/SEND_MESSAGE";

    private RequestMessage requestMessage;

    public SendMessageRequest(String token, RequestMessage requestMessage) {
        super(TYPE, token);
        this.requestMessage = requestMessage;
    }

    public RequestMessage getRequestMessage() {
        return requestMessage;
    }
}