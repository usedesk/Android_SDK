package ru.usedesk.sdk.domain.entity.exceptions;

public class ApiException extends UsedeskException {
    public ApiException() {
    }

    public ApiException(String message) {
        super(message);
    }
}
