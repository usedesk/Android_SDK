package ru.usedesk.knowledgebase_sdk.internal.di;

import android.content.Context;

import javax.inject.Inject;

import io.reactivex.annotations.NonNull;
import ru.usedesk.common_sdk.internal.appdi.InjectBox;
import ru.usedesk.knowledgebase_sdk.external.IKnowledgeBaseSdk;
import ru.usedesk.knowledgebase_sdk.external.entity.KnowledgeBaseConfiguration;

public class InstanceBox extends InjectBox {

    @Inject
    IKnowledgeBaseSdk knowledgeBaseSdk;

    public InstanceBox(@NonNull Context appContext, @NonNull KnowledgeBaseConfiguration knowledgeBaseConfiguration) {
        init(new MainModule(appContext, knowledgeBaseConfiguration));
    }

    public IKnowledgeBaseSdk getKnowledgeBaseSdk() {
        return knowledgeBaseSdk;
    }
}
