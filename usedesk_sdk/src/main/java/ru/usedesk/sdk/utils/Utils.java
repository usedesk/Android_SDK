package ru.usedesk.sdk.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.internal.LinkedTreeMap;

import ru.usedesk.sdk.models.Payload;

public class Utils {

    private Utils() {
    }

    public static Payload parsePayload(Object payload) {
        if (payload != null) {
            try {
                Gson gson = new Gson();
                if (payload instanceof LinkedTreeMap) {
                    JsonObject jsonObject = gson.toJsonTree(payload).getAsJsonObject();
                    if (jsonObject != null) {
                        return gson.fromJson(jsonObject, Payload.class);
                    } else {
                        return new Gson().fromJson(payload.toString(), Payload.class);
                    }
                } else {
                    return new Gson().fromJson(payload.toString(), Payload.class);
                }
            } catch (JsonSyntaxException e) {
                return new Payload();
            }
        } else {
            return new Payload();
        }
    }
}