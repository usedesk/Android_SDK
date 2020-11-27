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
import ru.usedesk.sample.databinding.ScreenInfoBinding;

public class InfoScreen extends Fragment {

    public static InfoScreen newInstance() {
        return new InfoScreen();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ScreenInfoBinding binding = DataBindingUtil.inflate(inflater, R.layout.screen_info,
                container, false);

        binding.ivBack.setOnClickListener(v -> {
            requireActivity().onBackPressed();
        });

        binding.tvSdkVer.setText("SDK v" + BuildConfig.VERSION_NAME);

        return binding.getRoot();
    }
}