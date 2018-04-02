package ru.usedesk.sdk.models;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class BaseRequest {

    private String type;

    public BaseRequest(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public JSONObject toJSONObject() throws JSONException {
        return new JSONObject(new Gson().toJson(this));
    }
}