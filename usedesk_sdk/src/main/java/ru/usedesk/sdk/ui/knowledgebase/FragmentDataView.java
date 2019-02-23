package ru.usedesk.sdk.ui.knowledgebase;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ru.usedesk.sdk.R;

public abstract class FragmentDataView<V, T extends DataViewModel<V>> extends FragmentView<T> {

    private final int layoutId;

    private TextView textViewMessage;

    private View container;

    public FragmentDataView(int layoutId) {
        this.layoutId = layoutId;
    }

    protected abstract void setDataView(V data);

    protected abstract ViewModelFactory<T> getViewModelFactory();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(layoutId, container, false);

        onView(view);

        initViewModel(getViewModelFactory());

        getViewModel().getLiveData()
                .observe(this, this::onData);

        return view;
    }

    protected void onView(@NonNull View view) {
        textViewMessage = view.findViewById(R.id.tv_message);
        container = view.findViewById(R.id.container);
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
        container.setVisibility(View.VISIBLE);
    }

    private void onMessage(int resourceId) {
        textViewMessage.setText(resourceId);

        textViewMessage.setVisibility(View.VISIBLE);
        container.setVisibility(View.GONE);
    }
}
