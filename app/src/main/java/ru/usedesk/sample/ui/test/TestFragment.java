package ru.usedesk.sample.ui.test;

import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ru.usedesk.sample.R;
import ru.usedesk.sample.databinding.FragmentTestBinding;
import ru.usedesk.sample.ui._common.BaseFragment;

public class TestFragment extends BaseFragment {

    public static Fragment newInstance() {
        return new TestFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        FragmentTestBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_test,
                container, false);

        TestViewModel viewModel = ViewModelProviders.of(this)
                .get(TestViewModel.class);

        bindTextInput(binding.tilEmail, viewModel::getEmailLiveData);
        bindTextInput(binding.tilPhoneNumber, viewModel::getPhoneNumberLiveData);

        return binding.getRoot();
    }
}
