package ru.usedesk.sdk.ui.knowledgebase.pages.articlebody;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import ru.usedesk.sdk.R;
import ru.usedesk.sdk.domain.entity.knowledgebase.ArticleBody;

public class ArticlesBodyAdapter extends RecyclerView.Adapter<ArticlesBodyAdapter.ArticleViewHolder> {

    private final List<ArticleBody> articleInfoList;
    private final IOnArticleBodyClickListener onArticleClickListener;

    ArticlesBodyAdapter(@NonNull List<ArticleBody> articleInfoList,
                        @NonNull IOnArticleBodyClickListener onArticleClickListener) {
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

        void bind(@NonNull final ArticleBody articleBody) {
            textViewTitle.setText(articleBody.getTitle());
            textViewCount.setText(Integer.toString(articleBody.getViews()));

            itemView.setOnClickListener(v -> onArticleClickListener.onArticleBodyClick(articleBody.getId()));
        }
    }
}
