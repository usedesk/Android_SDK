package ru.usedesk.sdk.appsdk;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.List;

import javax.inject.Inject;

import ru.usedesk.sdk.appsdk.di.ScopeSdk;
import ru.usedesk.sdk.domain.entity.Feedback;
import ru.usedesk.sdk.domain.entity.OfflineForm;
import ru.usedesk.sdk.domain.entity.UsedeskActionListener;
import ru.usedesk.sdk.domain.entity.UsedeskConfiguration;
import ru.usedesk.sdk.domain.entity.UsedeskFile;
import ru.usedesk.sdk.domain.interactor.UsedeskManager;
import toothpick.Toothpick;

public class UsedeskSDK {

    @Inject
    UsedeskManager usedeskManager;

    @Inject
    Context context;

    @Inject
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