package ru.usedesk.sdk.domain.entity.knowledgebase;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Category {

    private long id;
    private String title;
    @SerializedName("public")
    private int access;
    private int order;
    private List<ArticleInfo> articles;
}
