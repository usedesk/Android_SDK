package ru.usedesk.sdk.external.entity.knowledgebase;

public class ArticleInfo {
    private long id;
    private String title;
    private int views;

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public int getViews() {
        return views;
    }

    public void setViews(int views) {
        this.views = views;
    }
}
