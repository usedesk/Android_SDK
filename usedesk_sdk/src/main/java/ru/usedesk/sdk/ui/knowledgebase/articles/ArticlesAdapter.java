package ru.usedesk.sdk.ui.knowledgebase.articles;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import ru.usedesk.sdk.R;
import ru.usedesk.sdk.domain.entity.knowledgebase.ArticleInfo;

public class ArticlesAdapter extends RecyclerView.Adapter<ArticlesAdapter.SectionViewHolder> {

    private final List<ArticleInfo> articleInfoList;
    private final IOnArticleClickListener onArticleClickListener;

    ArticlesAdapter(@NonNull List<ArticleInfo> articleInfoList,
                    @NonNull IOnArticleClickListener onArticleClickListener) {
        this.articleInfoList = articleInfoList;
        this.onArticleClickListener = onArticleClickListener;
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
        sectionViewHolder.bind(articleInfoList.get(i));
    }

    @Override
    public int getItemCount() {
        return articleInfoList.size();
    }

    class SectionViewHolder extends RecyclerView.ViewHolder {

        private TextView textViewTitle;
        private TextView textViewText;

        SectionViewHolder(@NonNull View itemView) {
            super(itemView);

            textViewTitle = itemView.findViewById(R.id.tv_title);
            textViewText = itemView.findViewById(R.id.tv_text);
        }

        void bind(@NonNull final ArticleInfo articleInfo) {
            //imageViewIcon.setImageURI(section.getImage());//TODO:set icon
            textViewTitle.setText(articleInfo.getTitle());
            textViewText.setText("dbg_views: " + articleInfo.getViews());

            itemView.setOnClickListener(v -> onArticleClickListener.onArticleClick(articleInfo.getId()));
        }
    }
}
