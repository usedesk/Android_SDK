package ru.usedesk.sdk.external.entity.exceptions;

import android.support.annotation.NonNull;

import static ru.usedesk.sdk.external.entity.exceptions.UsedeskHttpException.Error.UNKNOWN_ERROR;

public class UsedeskHttpException extends UsedeskException {
    private final Error error;

    public UsedeskHttpException() {
        this.error = UNKNOWN_ERROR;
    }

    public UsedeskHttpException(String message) {
        super(message);
        this.error = UNKNOWN_ERROR;
    }

    public UsedeskHttpException(@NonNull Error error) {
        this.error = error;
    }

    public UsedeskHttpException(@NonNull Error error, String message) {
        super(message);
        this.error = error;
    }

    @NonNull
    public Error getError() {
        return error;
    }

    public enum Error {
        SERVER_ERROR,
        INVALID_TOKEN,
        ACCESS_ERROR,
        JSON_ERROR,
        IO_ERROR,
        UNKNOWN_ERROR
    }
}
