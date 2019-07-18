package ru.usedesk.sample.utils;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import ru.usedesk.sample.R;

public class ToolbarHelper {
    private final AppCompatActivity activity;
    private State state = State.BASE;

    public ToolbarHelper(AppCompatActivity activity) {
        this.activity = activity;
    }

    public void setToolbar() {
        Toolbar toolbar = activity.findViewById(R.id.toolbar);
        if (toolbar != null) {
            activity.setSupportActionBar(toolbar);
        }
    }

    public void update(@NonNull State state) {
        this.state = state;

        activity.invalidateOptionsMenu();

        if (activity.getSupportActionBar() != null) {
            activity.getSupportActionBar()
                    .setDisplayHomeAsUpEnabled(state.showHomeButton);
        }
    }

    public void setSearchButton(@NonNull MenuItem item) {
        item.setVisible(state.isShowSearchButton());
    }

    /*public void setSubtitle(final String subTitle) {
        ActionBar actionBar = activity.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setSubtitle(subTitle);
        }
    }*/

    public enum State {
        BASE(false, false),
        HOME(true, false),
        SEARCH(false, true),
        HOME_SEARCH(true, true);


        private final boolean showHomeButton;
        private final boolean showSearchButton;

        State(boolean showHomeButton, boolean showSearchButton) {
            this.showHomeButton = showHomeButton;
            this.showSearchButton = showSearchButton;
        }

        public boolean isShowHomeButton() {
            return showHomeButton;
        }

        public boolean isShowSearchButton() {
            return showSearchButton;
        }
    }
}