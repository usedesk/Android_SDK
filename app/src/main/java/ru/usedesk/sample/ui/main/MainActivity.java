package ru.usedesk.sample.ui.main;

import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;

import ru.usedesk.sample.R;
import ru.usedesk.sample.databinding.ActivityMainBinding;
import ru.usedesk.sample.model.configuration.entity.Configuration;
import ru.usedesk.sample.service.CustomForegroundNotificationsService;
import ru.usedesk.sample.service.CustomSimpleNotificationsService;
import ru.usedesk.sample.ui._common.Event;
import ru.usedesk.sample.ui._common.ToolbarHelper;
import ru.usedesk.sample.ui.screens.configuration.ConfigurationFragment;
import ru.usedesk.sample.ui.screens.info.InfoFragment;
import ru.usedesk.sdk.external.UsedeskSdk;
import ru.usedesk.sdk.external.entity.chat.UsedeskConfiguration;
import ru.usedesk.sdk.external.entity.knowledgebase.KnowledgeBaseConfiguration;
import ru.usedesk.sdk.external.ui.IUsedeskOnSearchQueryListener;
import ru.usedesk.sdk.external.ui.UsedeskViewCustomizer;
import ru.usedesk.sdk.external.ui.chat.ChatFragment;
import ru.usedesk.sdk.external.ui.knowledgebase.main.IOnUsedeskSupportClickListener;
import ru.usedesk.sdk.external.ui.knowledgebase.main.view.KnowledgeBaseFragment;

public class MainActivity extends AppCompatActivity
        implements ConfigurationFragment.IOnGoToSdkListener, IOnUsedeskSupportClickListener {

    private final ToolbarHelper toolbarHelper;
    private MainViewModel viewModel;

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
    }

    private void onNavigation(@NonNull Event<MainViewModel.Navigation> navigationEvent) {
        if (!navigationEvent.isProcessed()) {
            navigationEvent.onProcessed();
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
        }
    }

    private void onConfiguration(@NonNull Configuration configuration) {
        initUsedeskConfiguration(configuration);
        initUsedeskService(configuration);
        initUsedeskCustomizer(configuration);
    }

    private void initUsedeskConfiguration(@NonNull Configuration configuration) {
        UsedeskSdk.setUsedeskConfiguration(new UsedeskConfiguration(configuration.getCompanyId(),
                configuration.getEmail(),
                configuration.getUrl(),
                configuration.getOfflineFormUrl(),
                configuration.getClientName(),
                getLong(configuration.getClientPhoneNumber()),
                getLong(configuration.getClientAdditionalId())));

        if (configuration.isWithKnowledgeBase()) {
            UsedeskSdk.setKnowledgeBaseConfiguration(new KnowledgeBaseConfiguration(configuration.getAccountId(), configuration.getToken()));
        }
    }

    private void initUsedeskCustomizer(@NonNull Configuration configuration) {
        UsedeskViewCustomizer usedeskViewCustomizer = UsedeskSdk.getUsedeskViewCustomizer();
        if (configuration.isCustomViews()) {
            //Полная замена фрагментов кастомными
            usedeskViewCustomizer.setLayoutId(ru.usedesk.sdk.R.layout.usedesk_item_category, R.layout.custom_item_category);
            usedeskViewCustomizer.setLayoutId(ru.usedesk.sdk.R.layout.usedesk_item_section, R.layout.custom_item_section);
            usedeskViewCustomizer.setLayoutId(ru.usedesk.sdk.R.layout.usedesk_item_article_info, R.layout.custom_item_article_info);

            //Применение кастомной темы к стандартным фрагментам
            usedeskViewCustomizer.setThemeId(R.style.Usedesk_Theme_Custom);
        } else {
            usedeskViewCustomizer.setLayoutId(ru.usedesk.sdk.R.layout.usedesk_item_category, ru.usedesk.sdk.R.layout.usedesk_item_category);
            usedeskViewCustomizer.setLayoutId(ru.usedesk.sdk.R.layout.usedesk_item_section, ru.usedesk.sdk.R.layout.usedesk_item_section);
            usedeskViewCustomizer.setLayoutId(ru.usedesk.sdk.R.layout.usedesk_item_article_info, ru.usedesk.sdk.R.layout.usedesk_item_article_info);

            usedeskViewCustomizer.setThemeId(R.style.Usedesk_Theme);
        }
    }

    private void initUsedeskService(@NonNull Configuration configuration) {
        UsedeskSdk.getUsedeskNotificationsServiceFactory()
                .stopService(this);

        UsedeskSdk.setUsedeskNotificationsServiceFactory(configuration.isForegroundService()
                ? new CustomForegroundNotificationsService.Factory()
                : new CustomSimpleNotificationsService.Factory());
    }

    private Fragment getCurrentFragment() {
        return getSupportFragmentManager().findFragmentById(R.id.container_frame_layout);
    }

    private void goToConfiguration() {
        toolbarHelper.update(this, ToolbarHelper.State.CONFIGURATION);
        switchFragment(ConfigurationFragment.newInstance());
    }

    private void goToKnowledgeBase() {
        toolbarHelper.update(this, ToolbarHelper.State.KNOWLEDGE_BASE);
        switchFragment(KnowledgeBaseFragment.newInstance());
    }

    private void goToChat() {
        toolbarHelper.update(this, ToolbarHelper.State.CHAT);
        switchFragment(ChatFragment.newInstance());
    }

    private void goToInfo() {
        toolbarHelper.update(this, ToolbarHelper.State.INFO);
        switchFragment(InfoFragment.newInstance());
    }

    @Override
    public void onBackPressed() {
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

    private Long getLong(@Nullable String value) {
        return value == null || value.isEmpty()
                ? null
                : Long.valueOf(value);
    }

    @Override
    public void goToSdk() {
        viewModel.goSdk();
    }

    public void switchFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container_frame_layout, fragment)
                .commit();
    }
}