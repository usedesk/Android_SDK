package ru.usedesk.sdk.data.repository;

public interface ResponseProcessor<T> {

    T process(String rawResponse);
}