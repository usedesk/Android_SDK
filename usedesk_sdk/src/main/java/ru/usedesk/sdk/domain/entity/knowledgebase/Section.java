package ru.usedesk.sdk.domain.entity.knowledgebase;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Section {

    private long id;
    private String title;
    @SerializedName("public")
    private int access;
    private int order;
    private String image;
    private List<Category> categories;

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

    public List<Category> getCategories() {
        return categories;
    }
}
