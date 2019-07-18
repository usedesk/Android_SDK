package ru.usedesk.sdk.internal.domain.repositories.knowledge;

import android.support.annotation.NonNull;

import ru.usedesk.sdk.external.entity.exceptions.DataNotFoundException;
import ru.usedesk.sdk.external.entity.knowledgebase.KnowledgeBaseConfiguration;

public interface IKnowledgeBaseInfoRepository {

    @NonNull
    KnowledgeBaseConfiguration getConfiguration() throws DataNotFoundException;

    void setConfiguration(@NonNull KnowledgeBaseConfiguration configuration);
}
