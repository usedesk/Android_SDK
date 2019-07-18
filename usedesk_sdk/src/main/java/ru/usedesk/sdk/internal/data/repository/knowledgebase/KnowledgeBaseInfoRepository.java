package ru.usedesk.sdk.internal.data.repository.knowledgebase;

import android.support.annotation.NonNull;

import javax.inject.Inject;
import javax.inject.Named;

import ru.usedesk.sdk.external.entity.exceptions.DataNotFoundException;
import ru.usedesk.sdk.external.entity.knowledgebase.KnowledgeBaseConfiguration;
import ru.usedesk.sdk.internal.data.repository.user.info.DataLoader;
import ru.usedesk.sdk.internal.domain.repositories.knowledge.IKnowledgeBaseInfoRepository;

public class KnowledgeBaseInfoRepository implements IKnowledgeBaseInfoRepository {

    private DataLoader<KnowledgeBaseConfiguration> configurationDataLoader;

    @Inject
    public KnowledgeBaseInfoRepository(@Named("knowledgeBaseConfiguration") DataLoader<KnowledgeBaseConfiguration> configurationDataLoader) {
        this.configurationDataLoader = configurationDataLoader;
    }

    @NonNull
    @Override
    public KnowledgeBaseConfiguration getConfiguration() throws DataNotFoundException {
        return configurationDataLoader.getData();
    }

    @Override
    public void setConfiguration(@NonNull KnowledgeBaseConfiguration configuration) {
        configurationDataLoader.setData(configuration);
    }
}
