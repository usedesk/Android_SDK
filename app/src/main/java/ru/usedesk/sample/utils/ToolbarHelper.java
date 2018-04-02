package ru.usedesk.sample.utils;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import ru.usedesk.sample.R;

public class ToolbarHelper {

    private ToolbarHelper() {
    }

    public static void setToolbar(AppCompatActivity activity) {
        Toolbar toolbar = getToolbar(activity);
        if (toolbar != null) {
            activity.setSupportActionBar(toolbar);
        }
    }

    public static void setToolbarWithUpButton(AppCompatActivity activity) {
        setToolbar(activity);
        if (activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    public static void setSubtitle(final AppCompatActivity activity, final String subTitle) {
        Toolbar toolbar = getToolbar(activity);
        if (toolbar != null) {
            toolbar.post(new Runnable() {
                @Override
                public void run() {
                    activity.getSupportActionBar().setSubtitle(subTitle);
                }
            });
        }
    }

    private static Toolbar getToolbar(AppCompatActivity activity) {
        return (Toolbar) activity.findViewById(R.id.toolbar);
    }
}