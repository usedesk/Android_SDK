package ru.usedesk.sdk.ui.knowledgebase.helper;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

public class FragmentSwitcher {
    private final Fragment parentFragment;
    private final int idContainer;

    private Fragment lastFragment;

    public FragmentSwitcher(@NonNull Fragment parentFragment, int idContainer) {
        this.parentFragment = parentFragment;
        this.idContainer = idContainer;
    }

    public void switchFragment(@NonNull Fragment fragment) {
        this.lastFragment = fragment;

        getChildFragmentManager().beginTransaction()
                .addToBackStack("cur")
                .replace(idContainer, fragment)
                .commit();
    }

    @Nullable
    public Fragment getLastFragment() {
        return lastFragment;
    }

    public void setOnBackStackChangedListener(FragmentManager.OnBackStackChangedListener onFragmentStackSize) {
        getChildFragmentManager().removeOnBackStackChangedListener(onFragmentStackSize);
        getChildFragmentManager().addOnBackStackChangedListener(onFragmentStackSize);
    }

    private FragmentManager getChildFragmentManager() {
        return parentFragment.getChildFragmentManager();
    }

    public boolean onBackPressed() {
        if (getChildFragmentManager().getBackStackEntryCount() > 1) {
            getChildFragmentManager().popBackStack();
            return true;
        }
        return false;
    }

    public int getStackSize() {
        return getChildFragmentManager().getBackStackEntryCount();
    }
}