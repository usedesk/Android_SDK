package ru.usedesk.sample;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import ru.usedesk.sample.ui.ChatFragment;
import ru.usedesk.sample.ui.HomeFragment;
import ru.usedesk.sample.ui.InfoFragment;
import ru.usedesk.sdk.ui.knowledgebase.main.IOnSupportClickListener;
import ru.usedesk.sdk.ui.knowledgebase.main.KnowledgeBaseFragment;
import ru.usedesk.sdk.ui.knowledgebase.main.KnowledgeViewParent;

import static ru.usedesk.sample.utils.ToolbarHelper.setToolbar;

public class MainActivity extends AppCompatActivity
        implements BottomNavigationView.OnNavigationItemSelectedListener, IOnSupportClickListener {

    private BottomNavigationView bottomNavigationView;

    private KnowledgeViewParent knowledgeViewParent;
    private MainViewModel mainViewModel;

    public MainActivity() {
        knowledgeViewParent = new KnowledgeViewParent();
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
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.navigation_home:
                mainViewModel.onNavigate(MainViewModel.NAVIGATE_HOME);
                break;
            case R.id.knowledge_base:
                mainViewModel.onNavigate(MainViewModel.NAVIGATE_BASE);
                break;
            case R.id.navigation_info:
                mainViewModel.onNavigate(MainViewModel.NAVIGATE_INFO);
                break;
            default:
                return true;
        }

        return false;
    }

    private void onNavigate(int navigateId) {
        knowledgeViewParent.setOnSearchQueryListener(null);
        switch (navigateId) {
            case MainViewModel.NAVIGATE_HOME:
                switchFragment(HomeFragment.newInstance());
                break;
            case MainViewModel.NAVIGATE_BASE:
                KnowledgeBaseFragment knowledgeBaseFragment = KnowledgeBaseFragment.newInstance();
                knowledgeViewParent.setOnSearchQueryListener(knowledgeBaseFragment);
                switchFragment(knowledgeBaseFragment);
                break;
            case MainViewModel.NAVIGATE_INFO:
                switchFragment(InfoFragment.newInstance());
                break;
        }
        setToolbar(this);
    }

    private void initBottomNavigation() {
        bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (knowledgeViewParent.getOnSearchQueryListener() != null) {
            getMenuInflater().inflate(R.menu.toolbar_menu_with_search, menu);

            knowledgeViewParent.initSearch(menu, R.id.action_search);
        } else {
            getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        }

        return true;
    }

    @Override
    public void onSupportClick() {
        if (AppSession.getSession() != null) {
            switchFragment(ChatFragment.newInstance());
        }
    }

    private void switchFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container_frame_layout, fragment)
                .commit();
    }
}