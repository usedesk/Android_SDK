package ru.usedesk.sdk.external.entity.exceptions;

public abstract class UsedeskException extends Exception {
    public UsedeskException() {
    }

    public UsedeskException(String message) {
        super(message);
    }

    @Override
    public String toString() {
        return getClass().getName();
    }
}
