package ru.usedesk.common_sdk.external.entity.exceptions;

import android.util.Patterns;

import androidx.annotation.Nullable;

public class Validators {
    public static Boolean isValidEmail(@Nullable String text) {
        if (text == null) {
            return true;
        }
        return text.isEmpty() || isValidEmailNecessary(text);
    }

    public static Boolean isValidUrlNecessary(@Nullable String text) {
        return text != null
                && Patterns.WEB_URL.matcher(text).matches();
    }

    public static Boolean isValidEmailNecessary(@Nullable String text) {
        return text != null
                && Patterns.EMAIL_ADDRESS.matcher(text).matches();
    }

    public static Boolean isValidPhonePhone(@Nullable String text) {
        if (text == null) {
            return true;
        }
        return text.isEmpty()
                || (text.equals("+")
                || isValidPhoneNecessary(text));
    }

    public static Boolean isValidPhoneNecessary(@Nullable String text) {
        if (text == null) {
            return false;
        }
        String phone = text.replace(" ", "")
                .replace("-", "")
                .replace("(", "")
                .replace(")", "");

        return phone.length() >= 7
                && phone.length() <= 13
                && Patterns.PHONE
                .matcher(phone)
                .matches();
    }
}
