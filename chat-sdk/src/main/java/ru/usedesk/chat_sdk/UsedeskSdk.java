package ru.usedesk.chat_sdk;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import javax.inject.Inject;

import ru.usedesk.sdk.external.entity.chat.UsedeskActionListener;
import ru.usedesk.sdk.external.entity.chat.UsedeskConfiguration;
import ru.usedesk.sdk.external.entity.knowledgebase.KnowledgeBaseConfiguration;
import ru.usedesk.sdk.external.service.notifications.UsedeskNotificationsServiceFactory;
import ru.usedesk.sdk.external.ui.UsedeskViewCustomizer;
import ru.usedesk.sdk.internal.appdi.DependencyInjection;
import ru.usedesk.sdk.internal.appdi.ScopeChat;
import toothpick.Toothpick;

public class UsedeskSdk {

    private static UsedeskChatBox usedeskChatBox;
    private static UsedeskKnowledgeBaseBox usedeskKnowledgeBaseBox;

    private static UsedeskNotificationsServiceFactory usedeskNotificationsServiceFactory;
    private static UsedeskViewCustomizer usedeskViewCustomizer = new UsedeskViewCustomizer();

    private static UsedeskConfiguration usedeskConfiguration;
    private static KnowledgeBaseConfiguration knowledgeBaseConfiguration;

    @NonNull
    @Deprecated
    public static UsedeskChat initChat(@NonNull Context context,
                                       @NonNull UsedeskConfiguration usedeskConfiguration,
                                       @NonNull UsedeskActionListener usedeskActionListener) {
        if (usedeskChatBox == null) {
            usedeskChatBox = new UsedeskChatBox(context, usedeskConfiguration, usedeskActionListener);
        }

        return usedeskChatBox.usedeskChat;
    }

    @Deprecated
    @NonNull
    public static UsedeskChat initChat(@NonNull Context context,
                                       @NonNull UsedeskActionListener usedeskActionListener) {
        if (usedeskChatBox == null) {
            if (usedeskConfiguration == null) {
                throw new RuntimeException("Set UsedeskConfiguration before init");
            }
            usedeskChatBox = new UsedeskChatBox(context, usedeskConfiguration, usedeskActionListener);
        }

        return usedeskChatBox.usedeskChat;
    }

    @Deprecated
    @NonNull
    public static UsedeskChat getChat() {
        if (usedeskChatBox == null) {
            throw new RuntimeException("Must call UsedeskSdk.initChat() before this method");
        }
        return usedeskChatBox.usedeskChat;
    }

    @Deprecated
    public static void releaseChat() {
        if (usedeskChatBox != null) {
            usedeskChatBox.release();
            usedeskChatBox = null;
        }
    }

    @NonNull
    public static UsedeskViewCustomizer getUsedeskViewCustomizer() {
        return usedeskViewCustomizer;
    }

    @NonNull
    public static UsedeskKnowledgeBase initKnowledgeBase(@NonNull Context context) {
        if (usedeskKnowledgeBaseBox == null) {
            usedeskKnowledgeBaseBox = new UsedeskKnowledgeBaseBox(context);
            if (knowledgeBaseConfiguration != null) {
                usedeskKnowledgeBaseBox.usedeskKnowledgeBase.setKnowledgebaseConfiguration(knowledgeBaseConfiguration);
            }
        }

        return usedeskKnowledgeBaseBox.usedeskKnowledgeBase;
    }

    @NonNull
    public static UsedeskKnowledgeBase getUsedeskKnowledgeBase() {
        if (usedeskKnowledgeBaseBox == null) {
            throw new RuntimeException("Must call UsedeskSdk.initKnowledgeBase() before this method");
        }
        return usedeskKnowledgeBaseBox.usedeskKnowledgeBase;
    }

    public static void releaseUsedeskKnowledgeBase() {
        if (usedeskKnowledgeBaseBox != null) {
            usedeskKnowledgeBaseBox.release();
            usedeskKnowledgeBaseBox = null;
        }
    }

    public static UsedeskNotificationsServiceFactory getUsedeskNotificationsServiceFactory() {
        if (usedeskNotificationsServiceFactory == null) {
            usedeskNotificationsServiceFactory = new UsedeskNotificationsServiceFactory();
        }
        return usedeskNotificationsServiceFactory;
    }

    public static void setUsedeskNotificationsServiceFactory(UsedeskNotificationsServiceFactory usedeskNotificationsServiceFactory) {
        UsedeskSdk.usedeskNotificationsServiceFactory = usedeskNotificationsServiceFactory;
    }

    @Nullable
    public static UsedeskConfiguration getUsedeskConfiguration() {
        return usedeskConfiguration;
    }

    public static void setUsedeskConfiguration(@NonNull UsedeskConfiguration usedeskConfiguration) {
        UsedeskSdk.usedeskConfiguration = usedeskConfiguration;
    }

    public static void setKnowledgeBaseConfiguration(@NonNull KnowledgeBaseConfiguration knowledgeBaseConfiguration) {
        UsedeskSdk.knowledgeBaseConfiguration = knowledgeBaseConfiguration;
    }

    @SuppressWarnings("Injectable")
    static class UsedeskChatBox extends InjectBox {
        @Inject
        UsedeskChat usedeskChat;

        UsedeskChatBox(@NonNull Context context,
                       @NonNull UsedeskConfiguration usedeskConfiguration,
                       @NonNull UsedeskActionListener usedeskActionListener) {
            init(new ScopeChat(this, context));
            usedeskChat.init(usedeskConfiguration, usedeskActionListener);
        }

        @Override
        void release() {
            super.release();
            usedeskChat.destroy();
        }
    }

    @SuppressWarnings("Injectable")
    static class IUsedeskChatBox extends InjectBox {
        @Inject
        UsedeskChatSdk usedeskChat;

        @Inject
        IUsedeskChatRx usedeskChatRx;

        IUsedeskChatBox(@NonNull Context context) {
            init(new ScopeChat(this, context));
        }
    }

    static class InjectBox {
        private DependencyInjection dependencyInjection;

        void release() {
            Toothpick.closeScope(dependencyInjection.getScope());
        }

        void init(@NonNull DependencyInjection dependencyInjection) {
            this.dependencyInjection = dependencyInjection;
            Toothpick.inject(this, dependencyInjection.getScope());
        }
    }
}
