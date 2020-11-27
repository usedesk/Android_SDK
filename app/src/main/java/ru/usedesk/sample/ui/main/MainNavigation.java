package ru.usedesk.sample.ui.main;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import ru.usedesk.chat_gui.external.UsedeskChatFragment;
import ru.usedesk.chat_gui.external.showfile.UsedeskShowFileFragment;
import ru.usedesk.chat_gui.external.showhtml.UsedeskShowHtmlFragment;
import ru.usedesk.chat_sdk.internal.domain.entity.UsedeskFile;
import ru.usedesk.knowledgebase_gui.external.UsedeskKnowledgeBaseFragment;
import ru.usedesk.sample.ui.screens.configuration.ConfigurationScreen;
import ru.usedesk.sample.ui.screens.info.InfoScreen;

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
                .addToBackStack(fragment.getClass().getName())
                .replace(containerId, fragment)
                .commit();
    }

    void goConfiguration() {
        switchFragment(ConfigurationScreen.newInstance());
    }

    void goInfo() {
        switchFragment(InfoScreen.newInstance());
    }

    void goChat(@Nullable String customAgentName) {
        switchFragment(UsedeskChatFragment.newInstance(customAgentName));
    }

    void goKnowledgeBase() {
        switchFragment(UsedeskKnowledgeBaseFragment.newInstance());
    }

    void goShowFile(@NonNull UsedeskFile usedeskFile) {
        switchFragment(UsedeskShowFileFragment.newInstance(usedeskFile));
    }

    void goShowHtml(@NonNull String htmlText) {
        switchFragment(UsedeskShowHtmlFragment.newInstance(htmlText));
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
