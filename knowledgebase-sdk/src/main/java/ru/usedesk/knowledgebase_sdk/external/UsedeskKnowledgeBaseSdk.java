package ru.usedesk.knowledgebase_sdk.external;

import android.content.Context;
import android.support.annotation.NonNull;

import ru.usedesk.knowledgebase_sdk.external.entity.KnowledgeBaseConfiguration;
import ru.usedesk.knowledgebase_sdk.internal.di.InstanceBox;

public final class UsedeskKnowledgeBaseSdk {

    private InstanceBox instanceBox;

    public IKnowledgeBaseSdk init(@NonNull Context appContext,
                                  @NonNull KnowledgeBaseConfiguration configuration) {
        if (instanceBox == null) {
            instanceBox = new InstanceBox(appContext, configuration);
        }
        return instanceBox.getKnowledgeBaseSdk();
    }

    public IKnowledgeBaseSdk getInstance() {
        if (instanceBox == null) {
            throw new RuntimeException("Must call UsedeskKnowledgeBaseSdk.init(...) before");
        }
        return instanceBox.getKnowledgeBaseSdk();
    }

    public void release() {
        if (instanceBox != null) {
            instanceBox.release();
            instanceBox = null;
        }
    }
}
