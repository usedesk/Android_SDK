package ru.usedesk.sdk.domain.entity.knowledgebase;

import java.util.List;

public class Category {

    private long id;
    private String title;
    private int access;
    private int order;
    private List<Article> articles;
}
