package ru.usedesk.sdk.ui.knowledgebase.categories;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import ru.usedesk.sdk.R;
import ru.usedesk.sdk.domain.entity.knowledgebase.Category;

public class CategoriesAdapter extends RecyclerView.Adapter<CategoriesAdapter.SectionViewHolder> {

    private final List<Category> categoryList;
    private final IOnCategoryClickListener onCategoryClickListener;

    CategoriesAdapter(@NonNull List<Category> categoryList,
                      @NonNull IOnCategoryClickListener onCategoryClickListener) {
        this.categoryList = categoryList;
        this.onCategoryClickListener = onCategoryClickListener;
    }

    @NonNull
    @Override
    public SectionViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.section_item, viewGroup, false);

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

        private TextView textViewTitle;
        private TextView textViewText;

        SectionViewHolder(@NonNull View itemView) {
            super(itemView);

            textViewTitle = itemView.findViewById(R.id.tv_title);
            textViewText = itemView.findViewById(R.id.tv_text);
        }

        void bind(@NonNull final Category category,
                  @NonNull final IOnCategoryClickListener onCategoryClickListener) {
            //imageViewIcon.setImageURI(section.getImage());//TODO:set icon
            textViewTitle.setText(category.getTitle());
            textViewText.setText(category.getTitle());

            itemView.setOnClickListener(v -> onCategoryClickListener.onCategoryClick(category.getId()));
        }
    }
}
