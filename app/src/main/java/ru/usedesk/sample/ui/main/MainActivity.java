package ru.usedesk.sample.ui.main;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;

import ru.usedesk.sample.R;
import ru.usedesk.sample.databinding.ActivityMainBinding;
import ru.usedesk.sample.service.CustomForegroundNotificationsService;
import ru.usedesk.sample.service.CustomSimpleNotificationsService;
import ru.usedesk.sample.ui.screens.configuration.ConfigurationFragment;
import ru.usedesk.sample.ui.screens.configuration.ConfigurationModel;
import ru.usedesk.sample.ui.screens.configuration.ConfigurationRepository;
import ru.usedesk.sample.ui.screens.info.InfoFragment;
import ru.usedesk.sample.utils.ToolbarHelper;
import ru.usedesk.sdk.external.AppSession;
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

    private ToolbarHelper toolbarHelper;
    private boolean withKnowledgeBase = true;
    private ActivityMainBinding binding;

    public MainActivity() {
        toolbarHelper = new ToolbarHelper();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        toolbarHelper.initToolbar(this, binding.toolbar);

        if (savedInstanceState == null) {
            goToConfigure();
        }
    }

    private Fragment getCurrentFragment() {
        return getSupportFragmentManager().findFragmentById(R.id.container_frame_layout);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private void goToConfigure() {
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
        Fragment fragment = getCurrentFragment();

        if (fragment instanceof KnowledgeBaseFragment) {
            if (!((KnowledgeBaseFragment) fragment).onBackPressed()) {
                goToConfigure();
            }
        } else if (fragment instanceof ChatFragment) {
            if (withKnowledgeBase) {
                goToKnowledgeBase();
            } else {
                goToConfigure();
            }
        } else if (fragment instanceof InfoFragment) {
            goToConfigure();
        } else {
            super.onBackPressed();
        }
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
                goToInfo();
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

        ConfigurationModel configurationModel = new ConfigurationRepository().getConfigurationModel();

        this.withKnowledgeBase = configurationModel.isWithKnowledgeBase();

        UsedeskConfiguration usedeskConfiguration = new UsedeskConfiguration(configurationModel.getCompanyId(),
                configurationModel.getEmail(),
                configurationModel.getUrl(),
                configurationModel.getOfflineFormUrl(),
                configurationModel.getClientName(),
                Long.valueOf(configurationModel.getClientPhoneNumber()),
                Long.valueOf(configurationModel.getAccountId()));

        AppSession.startSession(usedeskConfiguration);

        UsedeskSdk.getUsedeskNotificationsServiceFactory()
                .stopService(this);

        UsedeskSdk.setUsedeskNotificationsServiceFactory(configurationModel.isForegroundService()
                ? new CustomForegroundNotificationsService.Factory()
                : new CustomSimpleNotificationsService.Factory());

        UsedeskViewCustomizer usedeskViewCustomizer = UsedeskSdk.getUsedeskViewCustomizer();
        if (configurationModel.isCustomViews()) {
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

        UsedeskSdk.initKnowledgeBase(this)
                .setConfiguration(new KnowledgeBaseConfiguration(configurationModel.getAccountId(), configurationModel.getToken()));
        UsedeskSdk.releaseUsedeskKnowledgeBase();

        if (this.withKnowledgeBase) {
            goToKnowledgeBase();
        } else {
            goToChat();
        }
    }


    private void switchFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container_frame_layout, fragment)
                .commit();
    }
}