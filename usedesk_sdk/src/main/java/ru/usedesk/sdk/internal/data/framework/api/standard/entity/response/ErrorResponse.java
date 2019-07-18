package ru.usedesk.sdk.internal.data.framework.api.standard.entity.response;

public class ErrorResponse extends BaseResponse {

    public static final String TYPE = "@@redbone/ERROR";

    private String message;
    private int code;

    public ErrorResponse() {
        super(TYPE);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}