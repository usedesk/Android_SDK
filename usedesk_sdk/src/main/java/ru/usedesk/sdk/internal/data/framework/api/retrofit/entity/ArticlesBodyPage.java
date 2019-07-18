package ru.usedesk.sdk.internal.data.framework.api.retrofit.entity;

import com.google.gson.annotations.SerializedName;

import ru.usedesk.sdk.external.entity.knowledgebase.ArticleBody;

public class ArticlesBodyPage {
    private int page;
    @SerializedName("last-page")
    private int lastPage;
    private int count;
    @SerializedName("total-count")
    private int totalCount;
    private ArticleBody[] articles;

    public int getPage() {
        return page;
    }

    public int getLastPage() {
        return lastPage;
    }

    public int getCount() {
        return count;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public ArticleBody[] getArticles() {
        return articles;
    }
}
