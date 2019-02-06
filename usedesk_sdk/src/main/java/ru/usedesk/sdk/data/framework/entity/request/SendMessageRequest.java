package ru.usedesk.sdk.data.framework.entity.request;

public class SendMessageRequest extends BaseRequest {

    public static final String TYPE = "@@server/chat/SEND_MESSAGE";

    private RequestMessage requestMessage;

    public SendMessageRequest() {
        super(TYPE);
    }

    public RequestMessage getRequestMessage() {
        return requestMessage;
    }

    public void setRequestMessage(RequestMessage requestMessage) {
        this.requestMessage = requestMessage;
    }

}