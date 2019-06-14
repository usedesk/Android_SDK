package ru.usedesk.sdk.external.ui.knowledgebase.helper;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import java.util.List;

public class FragmentSwitcher {

    public static void switchFragment(@Nullable Fragment parentFragment, @NonNull Fragment fragment,
                                      int idContainer) {
        if (parentFragment != null) {
            parentFragment.getChildFragmentManager()
                    .beginTransaction()
                    .addToBackStack("cur")
                    .replace(idContainer, fragment)
                    .commit();
        }
    }

    public static void setOnBackStackChangedListener(@Nullable Fragment parentFragment,
                                                     @NonNull FragmentManager.OnBackStackChangedListener onFragmentStackSize) {

        if (parentFragment != null) {
            parentFragment.getChildFragmentManager()
                    .removeOnBackStackChangedListener(onFragmentStackSize);

            parentFragment.getChildFragmentManager()
                    .addOnBackStackChangedListener(onFragmentStackSize);
        }
    }

    public static int getStackSize(@Nullable Fragment parentFragment) {
        if (parentFragment != null) {
            return parentFragment.getChildFragmentManager().getBackStackEntryCount();
        } else {
            return 0;
        }
    }

    public static boolean onBackPressed(@Nullable Fragment parentFragment) {
        if (parentFragment != null) {
            int count = parentFragment.getChildFragmentManager()
                    .getBackStackEntryCount();
            if (count > 0) {
                parentFragment.getChildFragmentManager()
                        .popBackStack();
                return true;
            }
        }
        return false;
    }

    @Nullable
    public static Fragment getLastFragment(@NonNull Fragment fragment) {
        List<Fragment> fragments = fragment.getChildFragmentManager()
                .getFragments();
        if (fragments.size() > 0) {
            return fragments.get(fragments.size() - 1);
        }

        return null;
    }
}