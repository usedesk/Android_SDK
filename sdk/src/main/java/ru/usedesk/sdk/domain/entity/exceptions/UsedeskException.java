package ru.usedesk.sdk.domain.entity.exceptions;

public abstract class UsedeskException extends Exception {
    public UsedeskException() {
    }

    public UsedeskException(String message) {
        super(message);
    }
}
