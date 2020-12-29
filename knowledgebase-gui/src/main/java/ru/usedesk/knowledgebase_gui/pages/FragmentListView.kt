package ru.usedesk.knowledgebase_gui.pages

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.usedesk.common_gui.UsedeskBinding
import ru.usedesk.knowledgebase_gui.R
import ru.usedesk.knowledgebase_gui.common.FragmentDataView

internal abstract class FragmentListView<DATA, BINDING : UsedeskBinding>(
        layoutId: Int,
        styleId: Int
) : FragmentDataView<List<DATA>, BINDING>(layoutId, styleId) {
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