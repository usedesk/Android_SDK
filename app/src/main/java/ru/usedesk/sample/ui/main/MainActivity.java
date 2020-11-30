package ru.usedesk.sample.ui.main;

import android.os.Bundle;
import android.os.StrictMode;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import org.jetbrains.annotations.NotNull;

import ru.usedesk.chat_gui.external.IUsedeskOnFileClickListener;
import ru.usedesk.chat_sdk.external.UsedeskChatSdk;
import ru.usedesk.chat_sdk.internal.domain.entity.UsedeskFile;
import ru.usedesk.knowledgebase_gui.external.IUsedeskOnBackPressedListener;
import ru.usedesk.knowledgebase_gui.external.IUsedeskOnSupportClickListener;
import ru.usedesk.sample.R;
import ru.usedesk.sample.databinding.ActivityMainBinding;
import ru.usedesk.sample.model.configuration.entity.Configuration;
import ru.usedesk.sample.service.CustomForegroundNotificationsService;
import ru.usedesk.sample.service.CustomSimpleNotificationsService;
import ru.usedesk.sample.ui._common.Event;
import ru.usedesk.sample.ui.screens.configuration.ConfigurationScreen;

public class MainActivity extends AppCompatActivity
        implements ConfigurationScreen.IOnGoToSdkListener,
        IUsedeskOnSupportClickListener,
        IUsedeskOnFileClickListener {

    private MainViewModel viewModel;
    private String customAgentName = null;
    private boolean inited = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().build());

        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        viewModel = ViewModelProviders.of(this).get(MainViewModel.class);

        viewModel.getConfigurationLiveData().observe(this, this::onConfiguration);

        viewModel.getErrorLiveData().observe(this, this::onError);

        if (!inited) {
            inited = true;

            viewModel.init(new MainNavigation(this, R.id.container));
        }
    }

    private void onError(@NonNull Event<String> error) {
        error.doEvent(data -> {
            Toast.makeText(this, error.getData(), Toast.LENGTH_LONG).show();
        });
    }

    private void onConfiguration(@NonNull Configuration configuration) {
        this.customAgentName = !configuration.getCustomAgentName().isEmpty()
                ? configuration.getCustomAgentName()
                : null;
        initUsedeskService(configuration);
        initUsedeskStyleManager(configuration);
    }

    private void initUsedeskStyleManager(@NonNull Configuration configuration) {
        if (configuration.isCustomViews()) {
            //Применение кастомной темы к стандартным фрагментам Чата
            //UsedeskStyleManager.replaceStyle(ru.usedesk.chat_gui.R.style.Usedesk_Theme_Chat, R.style.Usedesk_Theme_Chat_Custom);
        } else {
            //Сброс к стандартному gui
            //UsedeskStyleManager.replaceStyle(ru.usedesk.chat_gui.R.style.Usedesk_Theme_Chat, R.style.Usedesk_Theme_Chat);
        }
    }

    private void initUsedeskService(@NonNull Configuration configuration) {
        UsedeskChatSdk.stopService(this);

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