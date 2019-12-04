package ru.usedesk.sdk.internal.utils;

import android.util.Log;

import ru.usedesk.sdk.BuildConfig;

public class LogUtils {

    private static boolean LOGGING_ENABLED = !BuildConfig.BUILD_TYPE.equalsIgnoreCase("release");

    public static void LOGD(final String tag, String message) {
        if (LOGGING_ENABLED) {
            Log.d(tag, message);
        }
    }

    public static void LOGE(final String tag, String message) {
        Log.e(tag, message);
    }

    public static void LOGE(final String tag, Throwable cause) {
        Log.e(tag, cause.getMessage(), cause);
        cause.printStackTrace();
    }
}