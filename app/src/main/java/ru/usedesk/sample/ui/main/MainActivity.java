package ru.usedesk.sample.ui.main;

import android.os.Bundle;
import android.os.StrictMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import ru.usedesk.chat_gui.external.UsedeskChatFragment;
import ru.usedesk.chat_sdk.external.UsedeskChatSdk;
import ru.usedesk.common_gui.external.UsedeskStyleManager;
import ru.usedesk.knowledgebase_gui.external.IUsedeskOnBackPressedListener;
import ru.usedesk.knowledgebase_gui.external.IUsedeskOnSearchQueryListener;
import ru.usedesk.knowledgebase_gui.external.IUsedeskOnSupportClickListener;
import ru.usedesk.knowledgebase_gui.external.UsedeskKnowledgeBaseFragment;
import ru.usedesk.sample.R;
import ru.usedesk.sample.databinding.ActivityMainBinding;
import ru.usedesk.sample.model.configuration.entity.Configuration;
import ru.usedesk.sample.service.CustomForegroundNotificationsService;
import ru.usedesk.sample.service.CustomSimpleNotificationsService;
import ru.usedesk.sample.ui._common.Event;
import ru.usedesk.sample.ui._common.ToolbarHelper;
import ru.usedesk.sample.ui.screens.configuration.ConfigurationFragment;
import ru.usedesk.sample.ui.screens.info.InfoFragment;

public class MainActivity extends AppCompatActivity
        implements ConfigurationFragment.IOnGoToSdkListener, IUsedeskOnSupportClickListener {

    private final ToolbarHelper toolbarHelper;
    private MainViewModel viewModel;
    private String customAgentName = null;

    public MainActivity() {
        toolbarHelper = new ToolbarHelper();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().build());

        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        toolbarHelper.initToolbar(this, binding.toolbar);

        viewModel = ViewModelProviders.of(this).get(MainViewModel.class);

        viewModel.getNavigationLiveData()
                .observe(this, this::onNavigation);

        viewModel.getConfigurationLiveData()
                .observe(this, this::onConfiguration);

        viewModel.getErrorLiveData()
                .observe(this, this::onError);
    }

    private void onNavigation(@NonNull Event<MainViewModel.Navigation> navigationEvent) {
        navigationEvent.doEvent(navigation -> {
            switch (navigationEvent.getData()) {
                case EXIT:
                    finish();
                    break;
                case INFO:
                    goToInfo();
                    break;
                case CONFIGURATION:
                    goToConfiguration();
                    break;
                case SDK_KNOWLEDGE_BASE:
                    goToKnowledgeBase();
                    break;
                case SDK_CHAT:
                    goToChat();
                    break;
            }
        });
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
            UsedeskStyleManager.replaceStyle(ru.usedesk.chat_gui.R.style.Usedesk_Theme_Chat, R.style.Usedesk_Theme_Chat_Custom);
        } else {
            //Сброс к стандартному gui
            UsedeskStyleManager.replaceStyle(ru.usedesk.chat_gui.R.style.Usedesk_Theme_Chat, R.style.Usedesk_Theme_Chat);
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

    private void goToConfiguration() {
        toolbarHelper.update(this, ToolbarHelper.State.CONFIGURATION);
        switchFragment(ConfigurationFragment.newInstance());
    }

    private void goToKnowledgeBase() {
        toolbarHelper.update(this, ToolbarHelper.State.KNOWLEDGE_BASE);
        switchFragment(UsedeskKnowledgeBaseFragment.newInstance());
    }

    private void goToChat() {
        toolbarHelper.update(this, ToolbarHelper.State.CHAT);
        switchFragment(UsedeskChatFragment.newInstance(customAgentName));
    }

    private void goToInfo() {
        toolbarHelper.update(this, ToolbarHelper.State.INFO);
        switchFragment(InfoFragment.newInstance());
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = getCurrentFragment();
        if (fragment instanceof IUsedeskOnBackPressedListener
                && ((IUsedeskOnBackPressedListener) fragment).onBackPressed()) {
            return;
        }
        viewModel.goBack();
    }

    @Override
    public void onSupportClick() {
        goToChat();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.action_info:
                viewModel.goInfo();
                break;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        toolbarHelper.onCreateOptionsMenu(getMenuInflater(), menu);

        setSearchQueryListener(menu);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        toolbarHelper.onPrepareOptionsMenu(menu);

        return super.onPrepareOptionsMenu(menu);
    }

    private void setSearchQueryListener(Menu menu) {
        MenuItem menuItem = menu.findItem(R.id.action_search);
        if (menuItem != null) {
            menuItem.setVisible(true);

            SearchView searchView = (SearchView) menuItem.getActionView();
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    onQuery(query);
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String s) {
                    onQuery(s);
                    return false;
                }

                private void onQuery(String s) {
                    Fragment fragment = getCurrentFragment();
                    if (fragment instanceof IUsedeskOnSearchQueryListener) {
                        ((IUsedeskOnSearchQueryListener) fragment).onSearchQuery(s);
                    }
                }
            });
        }
    }

    @Override
    public void goToSdk() {
        viewModel.goSdk();
    }

    public void switchFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }
}