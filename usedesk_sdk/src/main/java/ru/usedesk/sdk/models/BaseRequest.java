package ru.usedesk.sdk.models;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class BaseRequest {

    private String type;
    private String token;

    public BaseRequest(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public JSONObject toJSONObject() throws JSONException {
        return new JSONObject(new Gson().toJson(this));
    }
}