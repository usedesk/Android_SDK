package ru.usedesk.chat_sdk.internal.data.framework.socket.entity.request;

public class SendMessageRequest extends BaseRequest {

    private static final String TYPE = "@@server/chat/SEND_MESSAGE";

    private final RequestMessage message;

    public SendMessageRequest(String token, RequestMessage requestMessage) {
        super(TYPE, token);
        this.message = requestMessage;
    }

    public RequestMessage getRequestMessage() {
        return message;
    }
}