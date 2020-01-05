package ru.usedesk.knowledgebase_sdk.internal.data.framework.retrofit.entity;

public class ViewsAdded {
    private final int res;
    private final int views;

    public ViewsAdded(int res, int views) {
        this.res = res;
        this.views = views;
    }

    public int getRes() {
        return res;
    }

    public int getViews() {
        return views;
    }
}
