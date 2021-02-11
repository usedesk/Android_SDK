package ru.usedesk.sample.ui.main;

import android.os.Bundle;
import android.os.StrictMode;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import org.jetbrains.annotations.NotNull;

import ru.usedesk.chat_gui.IUsedeskOnFileClickListener;
import ru.usedesk.chat_sdk.UsedeskChatSdk;
import ru.usedesk.chat_sdk.entity.UsedeskFile;
import ru.usedesk.common_gui.IUsedeskOnBackPressedListener;
import ru.usedesk.common_sdk.entity.UsedeskEvent;
import ru.usedesk.knowledgebase_gui.screens.IUsedeskOnSupportClickListener;
import ru.usedesk.sample.R;
import ru.usedesk.sample.databinding.ActivityMainBinding;
import ru.usedesk.sample.model.configuration.entity.Configuration;
import ru.usedesk.sample.service.CustomForegroundNotificationsService;
import ru.usedesk.sample.service.CustomSimpleNotificationsService;
import ru.usedesk.sample.ui.screens.configuration.ConfigurationScreen;

public class MainActivity extends AppCompatActivity
        implements ConfigurationScreen.IOnGoToSdkListener,
        IUsedeskOnSupportClickListener,
        IUsedeskOnFileClickListener {

    private MainViewModel viewModel;
    private String customAgentName = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().build());

        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        viewModel = ViewModelProviders.of(this).get(MainViewModel.class);

        viewModel.getConfigurationLiveData().observe(this, this::onConfiguration);

        viewModel.getErrorLiveData().observe(this, this::onError);

        viewModel.init(new MainNavigation(this, R.id.container));
    }

    private void onError(@NonNull UsedeskEvent<String> error) {
        error.process(text -> {
            Toast.makeText(this, text, Toast.LENGTH_LONG).show();
            return null;
        });
    }

    private void onConfiguration(@NonNull Configuration configuration) {
        this.customAgentName = !configuration.getCustomAgentName().isEmpty()
                ? configuration.getCustomAgentName()
                : null;
        initUsedeskService(configuration);
    }

    private void initUsedeskService(@NonNull Configuration configuration) {
        UsedeskChatSdk.setNotificationsServiceFactory(configuration.isForegroundService()
                ? new CustomForegroundNotificationsService.Factory()
                : new CustomSimpleNotificationsService.Factory());
    }

    private Fragment getCurrentFragment() {
        return getSupportFragmentManager().findFragmentById(R.id.container);
    }

    @Override
    public void onFileClick(@NotNull UsedeskFile usedeskFile) {
        viewModel.goShowFile(usedeskFile);
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = getCurrentFragment();
        if (fragment instanceof IUsedeskOnBackPressedListener
                && ((IUsedeskOnBackPressedListener) fragment).onBackPressed()) {
            //nothing
        } else {
            viewModel.onBackPressed();
        }
    }

    @Override
    public void onSupportClick() {
        viewModel.goChat(customAgentName);
    }

    @Override
    public void goToSdk() {
        viewModel.goSdk(customAgentName);
    }
}