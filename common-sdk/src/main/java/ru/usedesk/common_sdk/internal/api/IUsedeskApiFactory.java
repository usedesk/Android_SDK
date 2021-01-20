package ru.usedesk.common_sdk.internal.api;

public interface IUsedeskApiFactory {
    <API> API getInstance(String baseUrl, Class<API> apiClass);
}
