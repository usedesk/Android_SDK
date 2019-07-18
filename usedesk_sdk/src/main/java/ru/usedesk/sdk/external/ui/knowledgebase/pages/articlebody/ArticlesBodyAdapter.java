package ru.usedesk.sdk.external.ui.knowledgebase.pages.articlebody;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import ru.usedesk.sdk.R;
import ru.usedesk.sdk.external.entity.knowledgebase.ArticleBody;
import ru.usedesk.sdk.external.ui.ViewCustomizer;

public class ArticlesBodyAdapter extends RecyclerView.Adapter<ArticlesBodyAdapter.ArticleViewHolder> {

    private final List<ArticleBody> articleInfoList;
    private final IOnArticleBodyClickListener onArticleClickListener;
    private final ViewCustomizer viewCustomizer;

    ArticlesBodyAdapter(@NonNull List<ArticleBody> articleInfoList,
                        @NonNull IOnArticleBodyClickListener onArticleClickListener,
                        @NonNull ViewCustomizer viewCustomizer) {
        this.articleInfoList = articleInfoList;
        this.onArticleClickListener = onArticleClickListener;
        this.viewCustomizer = viewCustomizer;
    }

    @NonNull
    @Override
    public ArticleViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = viewCustomizer.createView(viewGroup, R.layout.usedesk_article_info_item);

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
