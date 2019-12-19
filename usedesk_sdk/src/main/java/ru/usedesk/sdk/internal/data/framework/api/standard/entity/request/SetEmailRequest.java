package ru.usedesk.sdk.internal.data.framework.api.standard.entity.request;

public class SetEmailRequest extends BaseRequest {

    public static final String TYPE = "@@server/chat/SET_EMAIL";

    private String email;
    private Payload payload;

    public SetEmailRequest(String token, String email, String name, Long phone, Long additionalId) {
        super(TYPE, token);
        this.email = email;
        this.payload = new Payload(name, phone, additionalId);
    }

    class Payload {
        private String name;
        private Long phone;
        private Long additionalId;
        private String email = "vatafak";

        Payload(String name, Long phone, Long additionalId) {
            this.name = name;
            this.phone = phone;
            this.additionalId = additionalId;
        }
    }
}