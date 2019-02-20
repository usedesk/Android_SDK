package ru.usedesk.sample;

import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import ru.usedesk.sample.ui.ChatFragment;
import ru.usedesk.sample.ui.HomeFragment;
import ru.usedesk.sample.ui.InfoFragment;
import ru.usedesk.sdk.ui.knowledgebase.main.KnowledgeBaseFragment;

import static ru.usedesk.sample.utils.ToolbarHelper.setToolbar;

public class MainActivity extends AppCompatActivity
        implements BottomNavigationView.OnNavigationItemSelectedListener {

    private BottomNavigationView bottomNavigationView;
    private Fragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setToolbar(this);
        initBottomNavigation();

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (currentFragment != null
                && bottomNavigationView.getSelectedItemId() == item.getItemId()) {
            return true;
        }

        currentFragment = null;

        switch (item.getItemId()) {
            case R.id.navigation_home:
                currentFragment = HomeFragment.newInstance();
                break;
            case R.id.knowledge_base:
                currentFragment = KnowledgeBaseFragment.newInstance();
                break;
            case R.id.navigation_chat:
                if (AppSession.getSession() != null) {
                    currentFragment = ChatFragment.newInstance();
                }
                break;
            case R.id.navigation_info:
                currentFragment = InfoFragment.newInstance();
                break;
        }

        if (currentFragment != null) {
            showCurrentFragment();
            return true;
        }

        return false;
    }

    private void initBottomNavigation() {
        bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        bottomNavigationView.setSelectedItemId(R.id.navigation_home);
    }

    private void showCurrentFragment() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container_frame_layout, currentFragment);
        fragmentTransaction.commit();
    }
}