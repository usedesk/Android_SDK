package ru.usedesk.knowledgebase_gui.internal.screens.helper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.List;

public class FragmentSwitcher {

    public static void switchFragment(@Nullable Fragment parentFragment,
                                      @NonNull Fragment fragment,
                                      int idContainer) {
        if (parentFragment != null) {
            parentFragment.getChildFragmentManager()
                    .beginTransaction()
                    .addToBackStack("cur")
                    .replace(idContainer, fragment)
                    .commit();
        }
    }

    public static boolean onBackPressed(@Nullable Fragment parentFragment) {
        if (parentFragment != null) {
            int count = parentFragment.getChildFragmentManager()
                    .getBackStackEntryCount();
            if (count > 1) {
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