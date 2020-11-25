package ru.usedesk.knowledgebase_sdk.external.entity;

public class UsedeskArticleInfo {
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
