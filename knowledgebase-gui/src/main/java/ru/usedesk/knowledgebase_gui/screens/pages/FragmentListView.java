package ru.usedesk.knowledgebase_gui.screens.pages;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ru.usedesk.knowledgebase_gui.R;
import ru.usedesk.knowledgebase_gui.screens.common.DataViewModel;
import ru.usedesk.knowledgebase_gui.screens.common.FragmentDataView;

public abstract class FragmentListView<V, T extends DataViewModel<List<V>>>
        extends FragmentDataView<List<V>, T> {

    private RecyclerView recyclerViewSections;

    public FragmentListView() {
        super(R.layout.usedesk_fragment_list);
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
