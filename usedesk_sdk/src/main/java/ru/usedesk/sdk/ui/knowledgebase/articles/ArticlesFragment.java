package ru.usedesk.sdk.ui.knowledgebase.articles;


import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import ru.usedesk.sdk.R;
import ru.usedesk.sdk.domain.entity.knowledgebase.ArticleInfo;

public class ArticlesFragment extends Fragment {

    private RecyclerView recyclerViewSections;
    private TextView textViewLoading;

    public ArticlesFragment() {
    }

    public static ArticlesFragment newInstance() {
        return new ArticlesFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sections, container, false);

        textViewLoading = view.findViewById(R.id.tv_loading);
        recyclerViewSections = view.findViewById(R.id.rv_list);

        ArticlesViewModel viewModel = ViewModelProviders.of(this)
                .get(ArticlesViewModel.class);//TODO: put key

        viewModel.getArticlesLiveData()
                .observe(this, this::onSectionsLoaded);

        return view;
    }

    private void onSectionsLoaded(List<ArticleInfo> articleInfos) {
        if (!(getParentFragment() instanceof IOnArticleClickListener)) {
            throw new RuntimeException("Parent fragment must implement " +
                    IOnArticleClickListener.class.getSimpleName());
        }
        ArticlesAdapter adapter = new ArticlesAdapter(articleInfos,
                (IOnArticleClickListener) getParentFragment());

        recyclerViewSections.setAdapter(adapter);
        recyclerViewSections.setLayoutManager(new LinearLayoutManager(getContext()));

        textViewLoading.setVisibility(View.GONE);
    }
}
