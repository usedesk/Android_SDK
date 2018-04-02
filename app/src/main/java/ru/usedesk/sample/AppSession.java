package ru.usedesk.sample;

import ru.usedesk.sdk.UsedeskConfiguration;

public class AppSession {

    private static AppSession activeSession;

    private UsedeskConfiguration usedeskConfiguration;

    private AppSession(UsedeskConfiguration usedeskConfiguration) {
        this.usedeskConfiguration = usedeskConfiguration;
    }

    public static void startSession(UsedeskConfiguration usedeskConfiguration) {
        activeSession = new AppSession(usedeskConfiguration);
    }

    public static AppSession getSession() {
        return activeSession;
    }

    public static void clearSession() {
        activeSession = null;
    }

    public synchronized UsedeskConfiguration getUsedeskConfiguration() {
        return usedeskConfiguration;
    }
}