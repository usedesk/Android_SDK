package ru.usedesk.sdk.external.entity.knowledgebase;

import com.google.gson.annotations.SerializedName;

public class ArticleBody {
    private long id;
    private String title;
    private String text;
    @SerializedName("public")
    private int access;
    private int order;
    private long categoryId;
    private long collectionId;
    private int views;
    private String createdAt;

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getText() {
        return text;
    }

    public int getAccess() {
        return access;
    }

    public int getOrder() {
        return order;
    }

    public long getCategoryId() {
        return categoryId;
    }

    public long getCollectionId() {
        return collectionId;
    }

    public int getViews() {
        return views;
    }

    public void setViews(int views) {
        this.views = views;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
