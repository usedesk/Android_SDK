package ru.usedesk.knowledgebase_sdk.external.entity;

import com.google.gson.annotations.SerializedName;

public class Section {

    private long id;
    private String title;
    @SerializedName("public")
    private int access;
    private int order;
    private String image;
    private Category[] categories;

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

    public String getImage() {
        return image;
    }

    public Category[] getCategories() {
        return categories;
    }
}
