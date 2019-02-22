package ru.usedesk.sdk.ui.knowledgebase.main;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;

public class KnowledgeViewParent {

    private IOnSearchQueryListener onSearchQueryListener;

    public IOnSearchQueryListener getOnSearchQueryListener() {
        return onSearchQueryListener;
    }

    public void setOnSearchQueryListener(@Nullable IOnSearchQueryListener onSearchQueryListener) {
        this.onSearchQueryListener = onSearchQueryListener;
    }

    public void initSearch(@NonNull Menu menu, int action_search) {
        MenuItem myActionMenuItem = menu.findItem(action_search);
        SearchView searchView = (SearchView) myActionMenuItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                onQuery(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                onQuery(s);
                return false;
            }

            private void onQuery(String s) {
                if (onSearchQueryListener != null) {
                    onSearchQueryListener.onSearchQuery(s);
                }
            }
        });
    }
}
