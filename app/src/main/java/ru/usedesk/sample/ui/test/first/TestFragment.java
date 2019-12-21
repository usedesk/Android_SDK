package ru.usedesk.sample.ui.test.first;

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
import ru.usedesk.sample.ui.main.MainActivity;
import ru.usedesk.sample.ui.test.TestModel;
import ru.usedesk.sample.ui.test.TestModel.Intent;
import ru.usedesk.sample.ui.test.TestModel.Key;
import ru.usedesk.sample.ui.test.second.TestSelectFragment;

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


        TestModel testModel = viewModel.getTestModel();

        bindTextInput(binding.tilEmail, testModel, Key.EMAIL_TEXT, Key.EMAIL_ERROR, Intent.EMAIL);
        bindTextInput(binding.tilPhoneNumber, testModel, Key.PHONE_NUMBER_TEXT, Key.PHONE_NUMBER_ERROR, Intent.PHONE_NUMBER);
        bindTextView(binding.btnSelect, testModel, Key.SELECT_TEXT);

        binding.btnSelect.setOnClickListener(v ->
                ((MainActivity) getActivity()).switchFragment(TestSelectFragment.newInstance()));

        return binding.getRoot();
    }
}
