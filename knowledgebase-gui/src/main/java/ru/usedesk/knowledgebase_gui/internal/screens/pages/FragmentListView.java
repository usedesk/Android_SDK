package ru.usedesk.knowledgebase_gui.internal.screens.pages;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ru.usedesk.knowledgebase_gui.R;
import ru.usedesk.knowledgebase_gui.internal.screens.common.DataViewModel;
import ru.usedesk.knowledgebase_gui.internal.screens.common.FragmentDataView;

public abstract class FragmentListView<V, T extends DataViewModel<List<V>>>
        extends FragmentDataView<List<V>, T> {

    private RecyclerView rvList;

    public FragmentListView() {
        super(R.layout.usedesk_fragment_list);
    }

    @Override
    protected void onView(@NonNull View view) {
        super.onView(view);
        rvList = view.findViewById(R.id.rv_list);
    }

    @Override
    protected void setDataView(List<V> data) {
        rvList.setAdapter(getAdapter(data));
        rvList.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    protected abstract RecyclerView.Adapter getAdapter(List<V> list);
}
