package ru.usedesk.sdk.domain.entity.exceptions;

public class DataNotFoundException extends UsedeskException {
    public DataNotFoundException() {
    }

    public DataNotFoundException(String message) {
        super(message);
    }
}
