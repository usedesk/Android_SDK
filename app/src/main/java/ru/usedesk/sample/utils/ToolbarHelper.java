package ru.usedesk.sample.utils;

import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import ru.usedesk.sample.R;

public class ToolbarHelper {

    public static void setToolbar(AppCompatActivity activity) {
        Toolbar toolbar = activity.findViewById(R.id.toolbar);
        if (toolbar != null) {
            activity.setSupportActionBar(toolbar);
        }
    }

    public static void setToolbar(AppCompatActivity activity, boolean showSearch, boolean showBack) {

    }


    private static void setToolbarUpButton(@NonNull AppCompatActivity activity, boolean show) {
        if (activity.getSupportActionBar() != null) {
            activity.getSupportActionBar()
                    .setDisplayHomeAsUpEnabled(show);
        }
    }

    public static void showToolbarUpButton(@NonNull AppCompatActivity activity) {
        setToolbarUpButton(activity, true);
    }

    public static void hideToolbarUpButton(@NonNull AppCompatActivity activity) {
        setToolbarUpButton(activity, false);
    }

    /*public static void setSearchButton(@NonNull AppCompatActivity activity, boolean show) {
        ActionBar actionBar = activity.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled();
            searchView.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
        }
    }*/

    public static void showSearchButton(SearchView searchView) {
        if (searchView != null) {
            searchView.setVisibility(View.VISIBLE);
        }
        //setSearchButton(activity, true);
    }

    public static void hideSearchButton(SearchView searchView) {
        if (searchView != null) {
            searchView.setVisibility(View.INVISIBLE);
        }
        //setSearchButton(activity, true);

    }

    public static void setSubtitle(final AppCompatActivity activity, final String subTitle) {
        ActionBar actionBar = activity.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setSubtitle(subTitle);
        }
    }
}