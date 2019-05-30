package ru.usedesk.sdk.external.entity.knowledgebase;

import android.support.annotation.NonNull;

import java.util.List;

public class SearchQuery {
    private final String searchQuery;
    private List<String> collectionIds;
    private List<String> categoryIds;
    private List<String> articleIds;
    private String count;
    private String page;
    private Type type;
    private Sort sort;
    private Order order;

    private SearchQuery(@NonNull String searchQuery) {
        this.searchQuery = searchQuery;
    }

    private static String collectionAsString(final List<String> list) {
        if (list != null && !list.isEmpty()) {
            StringBuilder builder = new StringBuilder();
            for (String item : list) {
                builder.append(item);
                builder.append(',');
            }
            builder.deleteCharAt(builder.length() - 1);
            return builder.toString();
        }
        return null;
    }

    @NonNull
    public String getSearchQuery() {
        return searchQuery;
    }

    public String getCollectionIds() {
        return collectionAsString(collectionIds);
    }

    public String getCategoryIds() {
        return collectionAsString(categoryIds);
    }

    public String getArticleIds() {
        return collectionAsString(articleIds);
    }

    public String getCount() {
        return count;
    }

    public String getPage() {
        return page;
    }

    public Type getType() {
        return type;
    }

    public Sort getSort() {
        return sort;
    }

    public Order getOrder() {
        return order;
    }

    public enum Type {
        PUBLIC, PRIVATE
    }

    public enum Sort {
        ID, TITLE, CATEGORY_ID, PUBLIC, CREATED_AT
    }

    public enum Order {
        ASCENDING, DESCENDING
    }

    static public class Builder {
        private final SearchQuery searchQuery;

        public Builder(@NonNull String searchQuery) {
            this.searchQuery = new SearchQuery(searchQuery);
        }

        public Builder setCollectionIds(List<String> collectionIds) {
            searchQuery.collectionIds = collectionIds;
            return this;
        }

        public Builder setCategoryIds(List<String> categoryIds) {
            searchQuery.categoryIds = categoryIds;
            return this;
        }

        public Builder setArticleIds(List<String> articleIds) {
            searchQuery.articleIds = articleIds;
            return this;
        }

        public Builder setCount(int count) {
            searchQuery.count = Integer.toString(count);
            return this;
        }

        public Builder setPage(int page) {
            searchQuery.page = Integer.toString(page);
            return this;
        }

        public Builder setType(Type type) {
            searchQuery.type = type;
            return this;
        }

        public Builder setSort(Sort sort) {
            searchQuery.sort = sort;
            return this;
        }

        public Builder setOrder(Order order) {
            searchQuery.order = order;
            return this;
        }

        public SearchQuery build() {
            return searchQuery;
        }
    }
}
