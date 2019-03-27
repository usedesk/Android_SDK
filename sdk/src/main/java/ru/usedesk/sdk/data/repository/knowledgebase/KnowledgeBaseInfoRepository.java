package ru.usedesk.sdk.data.repository.knowledgebase;

import android.support.annotation.NonNull;

import javax.inject.Inject;
import javax.inject.Named;

import ru.usedesk.sdk.data.repository.user.info.DataLoader;
import ru.usedesk.sdk.domain.boundaries.knowledge.IKnowledgeBaseInfoRepository;
import ru.usedesk.sdk.domain.entity.exceptions.DataNotFoundException;
import ru.usedesk.sdk.domain.entity.knowledgebase.KnowledgeBaseConfiguration;

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
