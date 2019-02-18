package ru.usedesk.sdk.appsdk;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.Single;
import ru.usedesk.sdk.appsdk.di.ScopeSdk;
import ru.usedesk.sdk.domain.entity.chat.Feedback;
import ru.usedesk.sdk.domain.entity.chat.OfflineForm;
import ru.usedesk.sdk.domain.entity.chat.UsedeskActionListener;
import ru.usedesk.sdk.domain.entity.chat.UsedeskConfiguration;
import ru.usedesk.sdk.domain.entity.chat.UsedeskFile;
import ru.usedesk.sdk.domain.entity.knowledgebase.ArticleBody;
import ru.usedesk.sdk.domain.entity.knowledgebase.ArticleInfo;
import ru.usedesk.sdk.domain.entity.knowledgebase.Section;
import ru.usedesk.sdk.domain.interactor.UsedeskManager;
import ru.usedesk.sdk.domain.interactor.knowledgebase.IKnowledgeBaseInteractor;
import toothpick.Toothpick;

public class UsedeskSDK {

    @Inject
    UsedeskManager usedeskManager;

    @Inject
    IKnowledgeBaseInteractor knowledgeBaseInteractor;

    @Inject
    Context context;

    UsedeskSDK(@NonNull Context context, UsedeskConfiguration configuration,
               UsedeskActionListener actionListener) {
        ScopeSdk scopeSdk = new ScopeSdk(this, context);
        Toothpick.inject(this, scopeSdk.getScope());

        usedeskManager.init(configuration, actionListener);
    }

    public void destroy() {
        usedeskManager.disconnect();
        usedeskManager = null;
    }

    public void sendMessage(String text, UsedeskFile usedeskFile) {
        usedeskManager.sendUserMessage(text, usedeskFile);
    }

    public void sendMessage(String text, List<UsedeskFile> usedeskFiles) {
        usedeskManager.sendUserMessage(text, usedeskFiles);
    }

    public void sendTextMessage(String text) {
        usedeskManager.sendUserTextMessage(text);
    }

    public void sendFileMessage(UsedeskFile usedeskFile) {
        usedeskManager.sendUserFileMessage(usedeskFile);
    }

    public void sendFeedbackMessage(Feedback feedback) {
        usedeskManager.sendFeedbackMessage(feedback);
    }

    public void sendOfflineForm(OfflineForm offlineForm) {
        usedeskManager.sendOfflineForm(offlineForm);
    }

    public void set(UsedeskConfiguration usedeskConfiguration,
                    UsedeskActionListener usedeskActionListener) {
        usedeskManager.updateUsedeskConfiguration(usedeskConfiguration);
        usedeskManager.updateUsedeskActionListener(usedeskActionListener);
        usedeskManager.reConnect();
    }

    public UsedeskConfiguration getUsedeskConfiguration() {
        return usedeskManager.getUsedeskConfiguration();
    }

    @NonNull
    public Single<List<Section>> getSectionsSingle() {
        return knowledgeBaseInteractor.getSectionsSingle();
    }

    @NonNull
    public Single<ArticleBody> getArticleSingle(@NonNull ArticleInfo articleInfo) {
        return knowledgeBaseInteractor.getArticleSingle(articleInfo);
    }

    public static class Builder {

        private Context context;
        private UsedeskConfiguration usedeskConfiguration;
        private UsedeskActionListener usedeskActionListener;

        public Builder(Context context) {
            this.context = context;
        }

        public Builder usedeskConfiguration(UsedeskConfiguration usedeskConfiguration) {
            this.usedeskConfiguration = usedeskConfiguration;

            return this;
        }

        public Builder usedeskActionListener(UsedeskActionListener usedeskActionListener) {
            this.usedeskActionListener = usedeskActionListener;

            return this;
        }

        public UsedeskSDK build() {
            if (usedeskConfiguration == null) {
                throw new NullPointerException("UsedeskConfiguration cannot be NULL!");
            }

            if (usedeskActionListener == null) {
                throw new NullPointerException("UsedeskActionListener cannot be NULL!");
            }

            return new UsedeskSDK(context, usedeskConfiguration, usedeskActionListener);
        }
    }
}