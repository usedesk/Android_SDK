package ru.usedesk.knowledgebase_gui.internal.screens.common;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import ru.usedesk.common_gui.external.UsedeskViewCustomizer;
import ru.usedesk.knowledgebase_gui.R;
import ru.usedesk.knowledgebase_gui.internal.screens.entity.DataOrMessage;

public abstract class FragmentDataView<V, T extends DataViewModel<V>> extends FragmentView<T> {

    private final int layoutId;

    private TextView textViewMessage;

    private RecyclerView rvList;

    public FragmentDataView(int layoutId) {
        this.layoutId = layoutId;
    }

    protected abstract void setDataView(V data);

    protected abstract ViewModelFactory<T> getViewModelFactory();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = UsedeskViewCustomizer.getInstance()
                .createView(inflater, layoutId, container, false, R.style.Usedesk_Theme_KnowledgeBase);

        onView(view);

        initViewModel(getViewModelFactory());

        getViewModel().getLiveData()
                .observe(getViewLifecycleOwner(), this::onData);

        return view;
    }

    protected void onView(@NonNull View view) {
        textViewMessage = view.findViewById(R.id.tv_message);
        rvList = view.findViewById(R.id.rv_list);
    }

    protected void onData(DataOrMessage<V> dataOrMessage) {
        switch (dataOrMessage.getMessage()) {
            case LOADING:
                onMessage(R.string.loading_title);
                break;
            case ERROR:
                onMessage(R.string.loading_error);
                break;
            default:
                onData(dataOrMessage.getData());
                break;
        }
    }

    protected void onData(V data) {
        setDataView(data);

        textViewMessage.setVisibility(View.GONE);
        rvList.setVisibility(View.VISIBLE);
    }

    private void onMessage(int resourceId) {
        textViewMessage.setText(resourceId);

        textViewMessage.setVisibility(View.VISIBLE);
        rvList.setVisibility(View.GONE);
    }
}
