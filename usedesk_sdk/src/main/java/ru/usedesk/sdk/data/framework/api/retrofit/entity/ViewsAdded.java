package ru.usedesk.sdk.data.framework.api.retrofit.entity;

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
