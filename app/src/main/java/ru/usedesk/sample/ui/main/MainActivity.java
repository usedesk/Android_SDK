package ru.usedesk.sample.ui.main;

import android.arch.lifecycle.ViewModelProviders;
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
import ru.usedesk.sdk.external.UsedeskSdk;
import ru.usedesk.sdk.external.ui.IUsedeskOnBackPressedListener;
import ru.usedesk.sdk.external.ui.IUsedeskOnSearchQueryListener;
import ru.usedesk.sdk.external.ui.ViewCustomizer;
import ru.usedesk.sdk.external.ui.chat.ChatFragment;
import ru.usedesk.sdk.external.ui.knowledgebase.main.IOnUsedeskSupportClickListener;
import ru.usedesk.sdk.external.ui.knowledgebase.main.view.KnowledgeBaseFragment;

import static ru.usedesk.sample.ui.main.MainViewModel.Navigate.BASE;
import static ru.usedesk.sample.ui.main.MainViewModel.Navigate.HOME;
import static ru.usedesk.sample.ui.main.MainViewModel.Navigate.INFO;
import static ru.usedesk.sample.utils.ToolbarHelper.setToolbarWithUpButton;

public class MainActivity extends AppCompatActivity
        implements BottomNavigationView.OnNavigationItemSelectedListener,
        IOnUsedeskSupportClickListener {

    private MainViewModel mainViewModel;

    public MainActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initBottomNavigation();

        mainViewModel = ViewModelProviders.of(this)
                .get(MainViewModel.class);

        mainViewModel.getNavigateLiveData()
                .observe(this, this::onNavigate);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        customizeView();
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
                mainViewModel.onNavigate(HOME);
                break;
            case R.id.knowledge_base:
                mainViewModel.onNavigate(BASE);
                break;
            case R.id.navigation_info:
                mainViewModel.onNavigate(INFO);
                break;
            default:
                return false;
        }

        return true;
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = getCurrentFragment();
        if (!(fragment instanceof IUsedeskOnBackPressedListener &&
                ((IUsedeskOnBackPressedListener) fragment).onBackPressed())) {
            super.onBackPressed();
        }
    }

    @Override
    public void onSupportClick() {
        switchFragment(ChatFragment.newInstance());
    }

    private void onNavigate(MainViewModel.Navigate navigate) {
        switch (navigate) {
            case HOME:
                switchFragment(HomeFragment.newInstance());
                break;
            case BASE:
                setToolbarWithUpButton(this);
                switchFragment(KnowledgeBaseFragment.newInstance());
                break;
            case INFO:
                switchFragment(InfoFragment.newInstance());
                break;
        }
        //setToolbar(this);
    }

    private void customizeView() {
        ViewCustomizer viewCustomizer = UsedeskSdk.initKnowledgeBase(this)
                .getViewCustomizer();

        viewCustomizer.setLayoutId(ru.usedesk.sdk.R.layout.usedesk_category_item, R.layout.category_item);
        viewCustomizer.setLayoutId(ru.usedesk.sdk.R.layout.usedesk_article_info_item, R.layout.article_info_item);
    }

    private void initBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                //getSupportActionBar().setDisplayShowHomeEnabled(false);
                break;
            case R.id.action_search:
                //getSupportActionBar().setDisplayShowHomeEnabled(true);
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