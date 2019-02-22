package ru.usedesk.sample;

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

    public MainActivity() {
        knowledgeViewParent = new KnowledgeViewParent();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initBottomNavigation();

        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.navigation_home);
        }

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        knowledgeViewParent.setOnSearchQueryListener(null);
        switch (item.getItemId()) {
            case R.id.navigation_home:
                switchFragment(HomeFragment.newInstance());
                break;
            case R.id.knowledge_base:
                KnowledgeBaseFragment knowledgeBaseFragment = KnowledgeBaseFragment.newInstance();
                knowledgeViewParent.setOnSearchQueryListener(knowledgeBaseFragment);
                switchFragment(knowledgeBaseFragment);
                break;
            case R.id.navigation_info:
                switchFragment(InfoFragment.newInstance());
                break;
            default:
                return false;
        }
        setToolbar(this);

        return true;
    }

    private void initBottomNavigation() {
        bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        bottomNavigationView.setSelectedItemId(R.id.navigation_home);
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