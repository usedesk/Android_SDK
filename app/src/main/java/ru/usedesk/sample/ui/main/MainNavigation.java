package ru.usedesk.sample.ui.main;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import ru.usedesk.chat_gui.chat.UsedeskChatScreen;
import ru.usedesk.chat_gui.showfile.UsedeskShowFileScreen;
import ru.usedesk.chat_sdk.entity.UsedeskFile;
import ru.usedesk.knowledgebase_gui.screens.main.UsedeskKnowledgeBaseScreen;
import ru.usedesk.sample.ui.screens.configuration.ConfigurationScreen;

public class MainNavigation {

    private final AppCompatActivity activity;
    private final int containerId;

    MainNavigation(@NonNull AppCompatActivity activity, int containerId) {
        this.activity = activity;
        this.containerId = containerId;
    }

    private void switchFragment(@NonNull Fragment fragment) {
        activity.getSupportFragmentManager()
                .beginTransaction()
                .addToBackStack(fragment.getClass().getName() + ":" + fragment.hashCode())
                .replace(containerId, fragment)
                .commit();
    }

    void goConfiguration() {
        switchFragment(ConfigurationScreen.newInstance());
    }

    void goChat(@Nullable String customAgentName) {
        switchFragment(UsedeskChatScreen.newInstance(customAgentName));
    }

    void goKnowledgeBase() {
        switchFragment(UsedeskKnowledgeBaseScreen.newInstance());
    }

    void goShowFile(@NonNull UsedeskFile usedeskFile) {
        switchFragment(UsedeskShowFileScreen.newInstance(usedeskFile));
    }

    public void onBackPressed() {
        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        if (fragmentManager.getBackStackEntryCount() > 1) {
            fragmentManager.popBackStack();
        } else {
            activity.finish();
        }
    }
}
