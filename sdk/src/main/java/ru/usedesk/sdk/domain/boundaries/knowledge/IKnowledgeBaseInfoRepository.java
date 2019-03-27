package ru.usedesk.sdk.domain.boundaries.knowledge;

import android.support.annotation.NonNull;

import ru.usedesk.sdk.domain.entity.exceptions.DataNotFoundException;
import ru.usedesk.sdk.domain.entity.knowledgebase.KnowledgeBaseConfiguration;

public interface IKnowledgeBaseInfoRepository {

    @NonNull
    KnowledgeBaseConfiguration getConfiguration() throws DataNotFoundException;

    void setConfiguration(@NonNull KnowledgeBaseConfiguration configuration);
}
