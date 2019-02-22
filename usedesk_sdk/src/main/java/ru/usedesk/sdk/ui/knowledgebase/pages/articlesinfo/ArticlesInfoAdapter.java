package ru.usedesk.sdk.ui.knowledgebase.pages.articlesinfo;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import ru.usedesk.sdk.R;
import ru.usedesk.sdk.domain.entity.knowledgebase.ArticleInfo;

public class ArticlesInfoAdapter extends RecyclerView.Adapter<ArticlesInfoAdapter.ArticleViewHolder> {

    private final List<ArticleInfo> articleInfoList;
    private final IOnArticleInfoClickListener onArticleClickListener;

    ArticlesInfoAdapter(@NonNull List<ArticleInfo> articleInfoList,
                        @NonNull IOnArticleInfoClickListener onArticleClickListener) {
        this.articleInfoList = articleInfoList;
        this.onArticleClickListener = onArticleClickListener;
    }

    @NonNull
    @Override
    public ArticleViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.article_info_item, viewGroup, false);

        return new ArticleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ArticleViewHolder articleViewHolder, int i) {
        articleViewHolder.bind(articleInfoList.get(i));
    }

    @Override
    public int getItemCount() {
        return articleInfoList.size();
    }

    class ArticleViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewTitle;
        private final TextView textViewCount;

        ArticleViewHolder(@NonNull View itemView) {
            super(itemView);

            textViewTitle = itemView.findViewById(R.id.tv_title);
            textViewCount = itemView.findViewById(R.id.tv_count);
        }

        void bind(@NonNull final ArticleInfo articleInfo) {
            textViewTitle.setText(articleInfo.getTitle());
            textViewCount.setText(Integer.toString(articleInfo.getViews()));

            itemView.setOnClickListener(v -> onArticleClickListener.onArticleInfoClick(articleInfo.getId()));
        }
    }
}
