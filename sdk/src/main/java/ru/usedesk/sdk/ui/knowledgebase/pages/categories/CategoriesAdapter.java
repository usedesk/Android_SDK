package ru.usedesk.sdk.ui.knowledgebase.pages.categories;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import ru.usedesk.sdk.R;
import ru.usedesk.sdk.domain.entity.knowledgebase.Category;
import ru.usedesk.sdk.ui.ViewCustomizer;

public class CategoriesAdapter extends RecyclerView.Adapter<CategoriesAdapter.SectionViewHolder> {

    private final List<Category> categoryList;
    private final IOnCategoryClickListener onCategoryClickListener;
    private final ViewCustomizer viewCustomizer;

    CategoriesAdapter(@NonNull List<Category> categoryList,
                      @NonNull IOnCategoryClickListener onCategoryClickListener,
                      @NonNull ViewCustomizer viewCustomizer) {
        this.categoryList = categoryList;
        this.onCategoryClickListener = onCategoryClickListener;
        this.viewCustomizer = viewCustomizer;
    }

    @NonNull
    @Override
    public SectionViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = viewCustomizer.createView(viewGroup, ViewCustomizer.Type.CATEGORY_ITEM);

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

        void bind(@NonNull final Category category,
                  @NonNull final IOnCategoryClickListener onCategoryClickListener) {
            textViewTitle.setText(category.getTitle());

            itemView.setOnClickListener(v -> onCategoryClickListener.onCategoryClick(category.getId()));
        }
    }
}
