package ru.usedesk.sdk.ui.knowledgebase.pages;

import android.arch.lifecycle.ViewModel;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import ru.usedesk.sdk.R;
import ru.usedesk.sdk.ui.knowledgebase.FragmentView;

public abstract class FragmentListView<V, T extends ViewModel> extends FragmentView<T> {

    private RecyclerView recyclerViewSections;
    private TextView textViewMessage;

    protected abstract void onData(List<V> list);

    protected abstract ListViewModel<V> initViewModel();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);

        textViewMessage = view.findViewById(R.id.tv_message);
        recyclerViewSections = view.findViewById(R.id.rv_list);

        initViewModel().getLiveData()
                .observe(this, this::onData);

        return view;
    }

    protected void initRecyclerView(RecyclerView.Adapter adapter) {
        recyclerViewSections.setAdapter(adapter);
        recyclerViewSections.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewSections.setVisibility(View.VISIBLE);
        textViewMessage.setVisibility(View.INVISIBLE);
    }

    protected void onData(ListOrMessage<V> listOrMessage) {
        switch (listOrMessage.getMessage()) {
            case LOADING:
                onMessage(R.string.loading_title);
                break;
            case ERROR:
                onMessage(R.string.loading_error);
                break;
            default:
                onData(listOrMessage.getDataList());
                break;
        }
    }

    private void onMessage(int resourceId) {
        textViewMessage.setText(resourceId);
        textViewMessage.setVisibility(View.VISIBLE);
        recyclerViewSections.setVisibility(View.GONE);
    }
}
