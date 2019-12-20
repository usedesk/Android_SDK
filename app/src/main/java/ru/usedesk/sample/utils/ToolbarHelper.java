package ru.usedesk.sample.utils;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;

import ru.usedesk.sample.R;

public class ToolbarHelper {
    private State state = State.CONFIGURATION;
    private Toolbar toolbar;

    public ToolbarHelper() {
    }

    public void initToolbar(@NonNull AppCompatActivity activity, @NonNull Toolbar toolbar) {
        this.toolbar = toolbar;
        activity.setSupportActionBar(toolbar);
        activity.invalidateOptionsMenu();
    }

    public void update(@NonNull AppCompatActivity activity, @NonNull State state) {
        this.state = state;

        activity.invalidateOptionsMenu();
        if (activity.getSupportActionBar() != null) {
            activity.getSupportActionBar()
                    .setDisplayHomeAsUpEnabled(state.showBackButton);

            activity.getSupportActionBar()
                    .setTitle(state.titleId);
        }
    }

    public boolean onCreateOptionsMenu(MenuInflater menuInflater, Menu menu) {
        menuInflater.inflate(R.menu.toolbar_menu_with_search, menu);

        return true;
    }

    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_search)
                .setVisible(state.showSearchButton);

        menu.findItem(R.id.action_info)
                .setVisible(state.showInfoButton);
    }

    public enum State {
        CONFIGURATION(R.string.configuration_title, false, false, true),
        INFO(R.string.info_title, true, false, false),
        KNOWLEDGE_BASE(R.string.knowledge_base_title, true, true, false),
        CHAT(R.string.chat_title, true, false, false);

        private final int titleId;
        private final boolean showBackButton;
        private final boolean showSearchButton;
        private final boolean showInfoButton;

        State(int titleId, boolean showBackButton, boolean showSearchButton, boolean showInfoButton) {
            this.titleId = titleId;
            this.showBackButton = showBackButton;
            this.showSearchButton = showSearchButton;
            this.showInfoButton = showInfoButton;
        }
    }
}