package ru.usedesk.knowledgebase_sdk.external;

import android.content.Context;

import androidx.annotation.NonNull;

import ru.usedesk.knowledgebase_sdk.external.entity.KnowledgeBaseConfiguration;
import ru.usedesk.knowledgebase_sdk.internal.di.InstanceBox;

public final class UsedeskKnowledgeBaseSdk {

    private static InstanceBox instanceBox;
    private static KnowledgeBaseConfiguration configuration;

    @NonNull
    public static IUsedeskKnowledgeBase init(@NonNull Context appContext) {
        if (instanceBox == null) {
            if (configuration == null) {
                throw new RuntimeException("Must call UsedeskKnowledgeBaseSdk.setConfiguration(...) before");
            }
            instanceBox = new InstanceBox(appContext, configuration);
        }
        return instanceBox.getKnowledgeBaseSdk();
    }

    @NonNull
    public static IUsedeskKnowledgeBase getInstance() {
        if (instanceBox == null) {
            throw new RuntimeException("Must call UsedeskKnowledgeBaseSdk.init(...) before");
        }
        return instanceBox.getKnowledgeBaseSdk();
    }

    public static void setConfiguration(@NonNull KnowledgeBaseConfiguration knowledgeBaseConfiguration) {
        configuration = knowledgeBaseConfiguration;
    }

    public static void release() {
        if (instanceBox != null) {
            instanceBox.release();
            instanceBox = null;
        }
    }
}
