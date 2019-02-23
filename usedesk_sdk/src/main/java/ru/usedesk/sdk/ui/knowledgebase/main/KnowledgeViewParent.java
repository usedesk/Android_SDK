package ru.usedesk.sdk.ui.knowledgebase.main;

import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;

public class KnowledgeViewParent {

    private KnowledgeBaseFragment knowledgeBaseFragment;
    private IOnSupportClickListener onSupportButtonListener;

    public boolean withChild() {
        return knowledgeBaseFragment != null;
    }

    public void attachChild(@NonNull KnowledgeBaseFragment knowledgeBaseFragment, ActionBar actionBar) {
        this.knowledgeBaseFragment = knowledgeBaseFragment;

        if (actionBar != null) {
            knowledgeBaseFragment.setOnFragmentStackSizeListener(
                    size -> actionBar.setDisplayHomeAsUpEnabled(size > 1));
        }

        knowledgeBaseFragment.setOnSupportClickListener(onSupportButtonListener);
    }

    public void detachChild() {
        if (knowledgeBaseFragment != null) {
            knowledgeBaseFragment.setOnFragmentStackSizeListener(null);
            knowledgeBaseFragment.setOnSupportClickListener(null);
        }
    }

    public boolean onBackPressed() {
        if (knowledgeBaseFragment != null) {
            return knowledgeBaseFragment.onBackPressed();
        }
        return false;
    }

    public void setOnSupportClickListener(IOnSupportClickListener onSupportButtonListener) {
        this.onSupportButtonListener = onSupportButtonListener;
    }

    public void onCreateOptionsMenu(@NonNull Menu menu, int searchId) {
        MenuItem menuItem = menu.findItem(searchId);
        if (menuItem != null) {
            menuItem.setVisible(withChild());

            if (withChild()) {
                SearchView searchView = (SearchView) menuItem.getActionView();
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
                        if (knowledgeBaseFragment != null) {
                            knowledgeBaseFragment.onSearchQuery(s);
                        }
                    }
                });
            }
        }
    }
}
