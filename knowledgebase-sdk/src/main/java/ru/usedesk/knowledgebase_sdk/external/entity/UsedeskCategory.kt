package ru.usedesk.knowledgebase_sdk.external.entity;

import com.google.gson.annotations.SerializedName;

public class UsedeskCategory {

    private long id;
    private String title;
    @SerializedName("public")
    private int access;
    private int order;
    private UsedeskArticleInfo[] articles;

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public int getAccess() {
        return access;
    }

    public int getOrder() {
        return order;
    }

    public UsedeskArticleInfo[] getArticles() {
        return articles;
    }
}
