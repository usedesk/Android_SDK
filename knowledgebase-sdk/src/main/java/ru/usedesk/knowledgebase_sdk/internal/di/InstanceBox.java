package ru.usedesk.knowledgebase_sdk.internal.di;

import android.content.Context;

import javax.inject.Inject;

import io.reactivex.annotations.NonNull;
import ru.usedesk.common_sdk.internal.appdi.InjectBox;
import ru.usedesk.knowledgebase_sdk.external.IUsedeskKnowledgeBase;
import ru.usedesk.knowledgebase_sdk.external.entity.KnowledgeBaseConfiguration;

@SuppressWarnings("injectable")
public class InstanceBox extends InjectBox {

    @Inject
    IUsedeskKnowledgeBase knowledgeBaseSdk;

    public InstanceBox(@NonNull Context appContext, @NonNull KnowledgeBaseConfiguration knowledgeBaseConfiguration) {
        init(new MainModule(appContext, knowledgeBaseConfiguration));
    }

    public IUsedeskKnowledgeBase getKnowledgeBaseSdk() {
        return knowledgeBaseSdk;
    }
}
