package ru.usedesk.sample.ui.main;

import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;

import ru.usedesk.sample.R;
import ru.usedesk.sample.ui.fragments.home.HomeFragment;
import ru.usedesk.sample.ui.fragments.info.InfoFragment;
import ru.usedesk.sample.utils.ToolbarHelper;
import ru.usedesk.sdk.external.UsedeskSdk;
import ru.usedesk.sdk.external.ui.IUsedeskOnSearchQueryListener;
import ru.usedesk.sdk.external.ui.ViewCustomizer;
import ru.usedesk.sdk.external.ui.chat.ChatFragment;
import ru.usedesk.sdk.external.ui.knowledgebase.main.IOnUsedeskSupportClickListener;
import ru.usedesk.sdk.external.ui.knowledgebase.main.view.KnowledgeBaseFragment;

public class MainActivity extends AppCompatActivity
        implements BottomNavigationView.OnNavigationItemSelectedListener,
        IOnUsedeskSupportClickListener {

    private ToolbarHelper toolbarHelper;
    private BottomNavigationView bottomNavigationView;

    public MainActivity() {
        toolbarHelper = new ToolbarHelper(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Usedesk_Theme_Custom);
        setContentView(R.layout.activity_main);
        initBottomNavigation();

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        customizeView();

        toolbarHelper.setToolbar();

        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.navigation_home);
        }
    }

    private Fragment getCurrentFragment() {
        return getSupportFragmentManager().findFragmentById(R.id.container_frame_layout);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.navigation_home:
                toolbarHelper.update(ToolbarHelper.State.BASE);
                switchFragment(HomeFragment.newInstance());
                break;
            case R.id.navigation_base:
                toolbarHelper.update(ToolbarHelper.State.HOME_SEARCH);
                switchFragment(KnowledgeBaseFragment.newInstance());
                break;
            case R.id.navigation_info:
                toolbarHelper.update(ToolbarHelper.State.HOME);
                switchFragment(InfoFragment.newInstance());
                break;
            default:
                return false;
        }

        return true;
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = getCurrentFragment();

        if (fragment instanceof KnowledgeBaseFragment) {
            if (!((KnowledgeBaseFragment) fragment).onBackPressed()) {
                bottomNavigationView.setSelectedItemId(R.id.navigation_home);
            }
        } else if (fragment instanceof ChatFragment || fragment instanceof InfoFragment) {
            bottomNavigationView.setSelectedItemId(R.id.navigation_base);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onSupportClick() {
        toolbarHelper.update(ToolbarHelper.State.HOME);
        switchFragment(ChatFragment.newInstance());
    }

    private void customizeView() {
        ViewCustomizer viewCustomizer = UsedeskSdk.initKnowledgeBase(this)
                .getViewCustomizer();

        viewCustomizer.setLayoutId(ru.usedesk.sdk.R.layout.usedesk_item_category, R.layout.category_item);
        viewCustomizer.setLayoutId(ru.usedesk.sdk.R.layout.usedesk_item_article_info, R.layout.article_info_item);
    }

    private void initBottomNavigation() {
        bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu_with_search, menu);

        setSearchQueryListener(menu);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        toolbarHelper.setSearchButton(menu.findItem(R.id.action_search));

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

    private void switchFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container_frame_layout, fragment)
                .commit();
    }
}