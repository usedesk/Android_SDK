package ru.usedesk.sdk.ui.knowledgebase.pages;

import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.List;

import ru.usedesk.sdk.R;
import ru.usedesk.sdk.ui.knowledgebase.common.DataViewModel;
import ru.usedesk.sdk.ui.knowledgebase.common.FragmentDataView;

public abstract class FragmentListView<V, T extends DataViewModel<List<V>>>
        extends FragmentDataView<List<V>, T> {

    private RecyclerView recyclerViewSections;

    public FragmentListView() {
        super(R.layout.fragment_list);
    }

    @Override
    protected void onView(@NonNull View view) {
        super.onView(view);
        recyclerViewSections = view.findViewById(R.id.rv_list);
    }

    @Override
    protected void setDataView(List<V> data) {
        recyclerViewSections.setAdapter(getAdapter(data));
        recyclerViewSections.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    protected abstract RecyclerView.Adapter getAdapter(List<V> list);
}
