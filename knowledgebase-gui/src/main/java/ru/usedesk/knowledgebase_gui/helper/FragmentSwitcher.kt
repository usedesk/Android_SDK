package ru.usedesk.knowledgebase_gui.helper

import androidx.fragment.app.Fragment

object FragmentSwitcher {
    fun switchFragment(parentFragment: Fragment?,
                       fragment: Fragment,
                       idContainer: Int) {
        parentFragment?.childFragmentManager?.beginTransaction()
                ?.addToBackStack("cur")
                ?.replace(idContainer, fragment)
                ?.commit()
    }

    fun onBackPressed(parentFragment: Fragment?): Boolean {
        if (parentFragment != null) {
            val count = parentFragment.childFragmentManager.backStackEntryCount
            if (count > 1) {
                parentFragment.childFragmentManager.popBackStack()
                return true
            }
        }
        return false
    }

    fun getLastFragment(fragment: Fragment): Fragment? {
        val fragments = fragment.childFragmentManager.fragments
        return if (fragments.isNotEmpty()) {
            fragments.last()
        } else null
    }
}