package ru.usedesk.sdk.models;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import org.json.JSONException;
import org.json.JSONObject;

import static ru.usedesk.sdk.Constants.KEY_COMPANY_ID;

public class OfflineForm {

    @SerializedName(KEY_COMPANY_ID)
    private String companyId;
    private String name;
    private String email;
    private String message;

    public OfflineForm() {
    }

    public OfflineForm(String companyId, String name, String email, String message) {
        this.companyId = companyId;
        this.name = name;
        this.email = email;
        this.message = message;
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String toJSONString() throws JSONException {
        return new JSONObject(new Gson().toJson(this)).toString();
    }
}