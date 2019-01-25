package ru.usedesk.sdk.data.framework.entity.request;

import com.google.gson.annotations.SerializedName;

import ru.usedesk.sdk.domain.entity.UsedeskFile;

import static ru.usedesk.sdk.domain.entity.Constants.KEY_FILE;

public class SendMessageRequest extends BaseRequest {

    public static final String TYPE = "@@server/chat/SEND_MESSAGE";

    private Message message;

    public SendMessageRequest() {
        super(TYPE);
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public static class Message {

        private String text;

        @SerializedName(KEY_FILE)
        private UsedeskFile usedeskFile;

        public Message() {
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public UsedeskFile getUsedeskFile() {
            return usedeskFile;
        }

        public void setUsedeskFile(UsedeskFile usedeskFile) {
            this.usedeskFile = usedeskFile;
        }
    }
}