package ru.usedesk.sample.ui.main;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import ru.usedesk.sample.R;
import ru.usedesk.sample.ui.fragments.home.HomeFragment;
import ru.usedesk.sample.ui.fragments.info.InfoFragment;
import ru.usedesk.sdk.appsdk.KnowledgeBase;
import ru.usedesk.sdk.ui.ViewCustomizer;
import ru.usedesk.sdk.ui.chat.ChatFragment;
import ru.usedesk.sdk.ui.knowledgebase.main.KnowledgeViewParent;
import ru.usedesk.sdk.ui.knowledgebase.main.view.KnowledgeBaseFragment;

import static ru.usedesk.sample.utils.ToolbarHelper.setToolbar;

public class MainActivity extends AppCompatActivity
        implements BottomNavigationView.OnNavigationItemSelectedListener {

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

        knowledgeViewParent.setOnSupportClickListener(() -> switchFragment(ChatFragment.newInstance()));

        if (savedInstanceState != null) {
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.container_frame_layout);
            if (fragment instanceof KnowledgeBaseFragment) {
                knowledgeViewParent.attachChild((KnowledgeBaseFragment) fragment, getSupportActionBar());
            }
        }
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
                return false;
        }

        return true;
    }

    @Override
    public void onBackPressed() {
        if (!knowledgeViewParent.onBackPressed()) {
            super.onBackPressed();
        }
    }

    private void onNavigate(int navigateId) {
        knowledgeViewParent.detachChild();
        switch (navigateId) {
            case MainViewModel.NAVIGATE_HOME:
                switchFragment(HomeFragment.newInstance());
                break;
            case MainViewModel.NAVIGATE_BASE:
                CustomizeView();
                KnowledgeBaseFragment knowledgeBaseFragment = KnowledgeBaseFragment.newInstance();
                knowledgeViewParent.attachChild(knowledgeBaseFragment, getSupportActionBar());
                switchFragment(knowledgeBaseFragment);
                break;
            case MainViewModel.NAVIGATE_INFO:
                switchFragment(InfoFragment.newInstance());
                break;
        }
        setToolbar(this);
    }

    private void CustomizeView() {
        ViewCustomizer viewCustomizer = KnowledgeBase.init(this)
                .getViewCustomizer();

        viewCustomizer.setLayoutId(ViewCustomizer.Type.CATEGORY_ITEM, R.layout.category_item);
        viewCustomizer.setLayoutId(ViewCustomizer.Type.ARTICLE_INFO_ITEM, R.layout.article_info_item);
    }

    private void initBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu_with_search, menu);

        knowledgeViewParent.onCreateOptionsMenu(menu, R.id.action_search);

        return true;
    }

    private void switchFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container_frame_layout, fragment)
                .commit();
    }
}