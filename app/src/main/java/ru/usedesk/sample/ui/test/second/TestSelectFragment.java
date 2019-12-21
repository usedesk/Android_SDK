package ru.usedesk.sample.ui.test.second;

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
import ru.usedesk.sample.databinding.FragmentTestSelectBinding;
import ru.usedesk.sample.ui._common.BaseFragment;
import ru.usedesk.sample.ui.main.MainActivity;
import ru.usedesk.sample.ui.test.TestModel;
import ru.usedesk.sample.ui.test.TestModel.Intent;
import ru.usedesk.sample.ui.test.TestModel.Key;
import ru.usedesk.sample.ui.test.first.TestFragment;
import ru.usedesk.sample.ui.test.first.TestViewModel;

public class TestSelectFragment extends BaseFragment {
    public static Fragment newInstance() {
        return new TestSelectFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        FragmentTestSelectBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_test_select,
                container, false);

        TestViewModel viewModel = ViewModelProviders.of(this)
                .get(TestViewModel.class);

        TestModel testModel = viewModel.getTestModel();

        bindTextView(binding.tvEmail, testModel, Key.EMAIL_TEXT);
        bindTextView(binding.tvPhoneNumber, testModel, Key.PHONE_NUMBER_TEXT);
        bindTextInput(binding.tilSelect, testModel, Key.SELECT_TEXT, Key.SELECT_ERROR, Intent.SELECT);

        binding.btnBack.setOnClickListener(v ->
                ((MainActivity) getActivity()).switchFragment(TestFragment.newInstance()));

        return binding.getRoot();
    }
}
