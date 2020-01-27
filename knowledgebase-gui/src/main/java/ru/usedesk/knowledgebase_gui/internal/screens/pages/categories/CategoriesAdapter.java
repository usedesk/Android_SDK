package ru.usedesk.knowledgebase_gui.internal.screens.pages.categories;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ru.usedesk.common_gui.external.IUsedeskViewCustomizer;
import ru.usedesk.knowledgebase_gui.R;
import ru.usedesk.knowledgebase_sdk.external.entity.UsedeskCategory;


public class CategoriesAdapter extends RecyclerView.Adapter<CategoriesAdapter.SectionViewHolder> {

    private final List<UsedeskCategory> categoryList;
    private final IOnCategoryClickListener onCategoryClickListener;
    private final IUsedeskViewCustomizer usedeskViewCustomizer;

    CategoriesAdapter(@NonNull List<UsedeskCategory> categoryList,
                      @NonNull IOnCategoryClickListener onCategoryClickListener,
                      @NonNull IUsedeskViewCustomizer usedeskViewCustomizer) {
        this.categoryList = categoryList;
        this.onCategoryClickListener = onCategoryClickListener;
        this.usedeskViewCustomizer = usedeskViewCustomizer;
    }

    @NonNull
    @Override
    public SectionViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = usedeskViewCustomizer.createView(viewGroup, R.layout.usedesk_item_category, R.style.Usedesk_Theme_KnowledgeBase);

        return new SectionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SectionViewHolder sectionViewHolder, int i) {
        sectionViewHolder.bind(categoryList.get(i), onCategoryClickListener);
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    class SectionViewHolder extends RecyclerView.ViewHolder {

        private final TextView textViewTitle;

        SectionViewHolder(@NonNull View itemView) {
            super(itemView);

            textViewTitle = itemView.findViewById(R.id.tv_title);
        }

        void bind(@NonNull final UsedeskCategory category,
                  @NonNull final IOnCategoryClickListener onCategoryClickListener) {
            textViewTitle.setText(category.getTitle());

            itemView.setOnClickListener(v -> onCategoryClickListener.onCategoryClick(category.getId()));
        }
    }
}
