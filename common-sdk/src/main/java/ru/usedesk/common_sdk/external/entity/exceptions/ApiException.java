package ru.usedesk.common_sdk.external.entity.exceptions;

@Deprecated
public class ApiException extends UsedeskException {
    public ApiException() {
    }

    public ApiException(String message) {
        super(message);
    }
}
