package ru.usedesk.knowledgebase_gui.screens.pages.articlesinfo;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ru.usedesk.common_gui.external.IUsedeskViewCustomizer;
import ru.usedesk.knowledgebase_gui.R;
import ru.usedesk.knowledgebase_sdk.external.entity.ArticleInfo;

public class ArticlesInfoAdapter extends RecyclerView.Adapter<ArticlesInfoAdapter.ArticleViewHolder> {

    private final List<ArticleInfo> articleInfoList;
    private final IOnArticleInfoClickListener onArticleClickListener;
    private final IUsedeskViewCustomizer usedeskViewCustomizer;

    ArticlesInfoAdapter(@NonNull List<ArticleInfo> articleInfoList,
                        @NonNull IOnArticleInfoClickListener onArticleClickListener,
                        @NonNull IUsedeskViewCustomizer usedeskViewCustomizer) {
        this.articleInfoList = articleInfoList;
        this.onArticleClickListener = onArticleClickListener;
        this.usedeskViewCustomizer = usedeskViewCustomizer;
    }

    @NonNull
    @Override
    public ArticleViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = usedeskViewCustomizer.createView(viewGroup, R.layout.usedesk_item_article_info);

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
