package ru.usedesk.knowledgebase_gui.internal.screens.pages

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.usedesk.knowledgebase_gui.R
import ru.usedesk.knowledgebase_gui.internal.screens.common.DataViewModel
import ru.usedesk.knowledgebase_gui.internal.screens.common.FragmentDataView

abstract class FragmentListView<V, T : DataViewModel<List<V>?>?> : FragmentDataView<List<V>?, T>(R.layout.usedesk_fragment_list) {
    private var recyclerViewSections: RecyclerView? = null
    override fun onView(view: View) {
        super.onView(view)
        recyclerViewSections = view.findViewById(R.id.rv_list)
    }

    protected override fun setDataView(data: List<V>) {
        recyclerViewSections!!.adapter = getAdapter(data)
        recyclerViewSections!!.layoutManager = LinearLayoutManager(context)
    }

    protected abstract fun getAdapter(list: List<V>?): RecyclerView.Adapter<*>?
}