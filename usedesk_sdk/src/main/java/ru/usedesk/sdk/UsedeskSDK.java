package ru.usedesk.sdk;

import android.content.Context;

import java.util.List;

import ru.usedesk.sdk.models.Feedback;
import ru.usedesk.sdk.models.OfflineForm;
import ru.usedesk.sdk.models.UsedeskFile;

public class UsedeskSDK {

    private Context context;
    private UsedeskManager usedeskManager;

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

    private UsedeskSDK(Context context) {
        this.context = context;
    }

    private void set(UsedeskConfiguration usedeskConfiguration, UsedeskActionListener usedeskActionListener) {
        if (usedeskManager == null) {
            usedeskManager = new UsedeskManager(context, usedeskConfiguration, usedeskActionListener);
        } else {
            usedeskManager.updateUsedeskConfiguration(usedeskConfiguration);
            usedeskManager.updateUsedeskActionListener(usedeskActionListener);
            usedeskManager.reConnect();
        }
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

            UsedeskSDK usedeskSDK = new UsedeskSDK(context);
            usedeskSDK.set(usedeskConfiguration, usedeskActionListener);
            return usedeskSDK;
        }
    }
}