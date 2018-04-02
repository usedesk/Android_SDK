package ru.usedesk.sdk;

public interface ResponseProcessor<T> {

    T process(String rawResponse);
}