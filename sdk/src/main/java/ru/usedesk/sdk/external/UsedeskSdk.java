package ru.usedesk.sdk.external;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.Observable;
import ru.usedesk.sdk.external.entity.chat.Message;
import ru.usedesk.sdk.external.entity.chat.UsedeskActionListener;
import ru.usedesk.sdk.external.entity.chat.UsedeskConfiguration;
import ru.usedesk.sdk.external.service.notifications.UsedeskNotificationsServiceFactory;
import ru.usedesk.sdk.internal.appdi.DependencyInjection;
import ru.usedesk.sdk.internal.appdi.ScopeChat;
import ru.usedesk.sdk.internal.appdi.ScopeKnowledgeBase;
import toothpick.Toothpick;

import static ru.usedesk.sdk.external.entity.chat.MessageType.OPERATOR_TO_CLIENT;

public class UsedeskSdk {

    private static UsedeskChatBox usedeskChatBox;
    private static UsedeskKnowledgeBaseBox usedeskKnowledgeBaseBox;
    private static UsedeskNotificationsServiceFactory usedeskNotificationsServiceFactory;

    @NonNull
    public static UsedeskChat initChat(@NonNull Context context,
                                       @NonNull UsedeskConfiguration usedeskConfiguration,
                                       @NonNull UsedeskActionListener usedeskActionListener) {
        if (usedeskChatBox == null) {
            usedeskChatBox = new UsedeskChatBox(context,
                    usedeskConfiguration, usedeskActionListener);
        }

        Observable.interval(5, 5, TimeUnit.SECONDS)
                .map(aLong -> new Message(OPERATOR_TO_CLIENT, "Ok"))
                .map(message -> {
                    message.setOperator("Виталий");
                    return message;
                })
                .subscribe(usedeskActionListener::onMessageReceived);

        return usedeskChatBox.usedeskChat;
    }

    @NonNull
    public static UsedeskChat getChat() {
        if (usedeskChatBox == null) {
            throw new RuntimeException("Must call UsedeskSdk.initChat() before this method");
        }
        return usedeskChatBox.usedeskChat;
    }

    public static void releaseChat() {
        if (usedeskChatBox != null) {
            usedeskChatBox.release();
            usedeskChatBox = null;
        }
    }

    @NonNull
    public static UsedeskKnowledgeBase initKnowledgeBase(@NonNull Context context) {
        if (usedeskKnowledgeBaseBox == null) {
            usedeskKnowledgeBaseBox = new UsedeskKnowledgeBaseBox(context);
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

    static class UsedeskKnowledgeBaseBox extends InjectBox {
        @Inject
        UsedeskKnowledgeBase usedeskKnowledgeBase;

        UsedeskKnowledgeBaseBox(@NonNull Context context) {
            init(new ScopeKnowledgeBase(this, context));
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
