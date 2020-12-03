package ru.usedesk.knowledgebase_gui.pages

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.usedesk.knowledgebase_gui.R
import ru.usedesk.knowledgebase_gui.common.FragmentDataView

abstract class FragmentListView<DATA>(
        layoutId: Int,
        defaultThemeId: Int
) : FragmentDataView<List<DATA>>(layoutId, defaultThemeId) {
    private lateinit var recyclerViewSections: RecyclerView

    override fun onView(view: View) {
        super.onView(view)
        recyclerViewSections = view.findViewById(R.id.rv_list)
    }

    override fun setDataView(data: List<DATA>) {
        recyclerViewSections.adapter = getAdapter(data)
        recyclerViewSections.layoutManager = LinearLayoutManager(context)
    }

    protected abstract fun getAdapter(list: List<DATA>): RecyclerView.Adapter<*>
}