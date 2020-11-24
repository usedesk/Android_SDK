package ru.usedesk.sample.ui.screens.info;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import ru.usedesk.sample.BuildConfig;
import ru.usedesk.sample.R;
import ru.usedesk.sample.databinding.FragmentInfoBinding;

public class InfoFragment extends Fragment {

    public static InfoFragment newInstance() {
        return new InfoFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentInfoBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_info,
                container, false);

        binding.tvSdkVer.setText("SDK v" + BuildConfig.VERSION_NAME);

        return binding.getRoot();
    }
}