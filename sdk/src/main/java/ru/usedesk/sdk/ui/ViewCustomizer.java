package ru.usedesk.sdk.ui;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.HashMap;

import javax.inject.Inject;

import ru.usedesk.sdk.R;

public class ViewCustomizer {

    private final HashMap<Type, Integer> layoutIds = new HashMap<Type, Integer>() {{
        put(Type.ARTICLE_INFO_ITEM, R.layout.article_info_item);
        put(Type.CATEGORY_ITEM, R.layout.category_item);
    }};

    @Inject
    public ViewCustomizer() {
    }

    public View createView(@NonNull ViewGroup viewGroup, @NonNull Type type) {
        int resourceId = getLayoutId(type);

        return LayoutInflater.from(viewGroup.getContext())
                .inflate(resourceId, viewGroup, false);
    }

    public int getLayoutId(@NonNull Type type) {
        Integer id = layoutIds.get(type);
        if (id == null) {
            throw new RuntimeException("Resource ID is not contains for this type");
        } else {
            return id;
        }
    }

    public void setLayoutId(@NonNull Type type, int resourceId) {
        layoutIds.put(type, resourceId);
    }

    public enum Type {
        ARTICLE_INFO_ITEM,
        CATEGORY_ITEM
    }
}
