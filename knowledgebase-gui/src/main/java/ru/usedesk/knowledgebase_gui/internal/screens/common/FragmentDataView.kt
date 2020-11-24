package ru.usedesk.knowledgebase_gui.internal.screens.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import ru.usedesk.common_gui.external.UsedeskViewCustomizer
import ru.usedesk.knowledgebase_gui.R
import ru.usedesk.knowledgebase_gui.internal.screens.entity.DataOrMessage

abstract class FragmentDataView<V, T : DataViewModel<V>?>(private val layoutId: Int) : FragmentView<T>() {
    private var textViewMessage: TextView? = null
    private var container: View? = null
    protected abstract fun setDataView(data: V)
    protected abstract val viewModelFactory: ViewModelFactory<T>?
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = UsedeskViewCustomizer.getInstance()
                .createView(inflater, layoutId, container, false, R.style.Usedesk_Theme_KnowledgeBase)
        onView(view)
        initViewModel(viewModelFactory!!)
        viewModel!!.liveData
                .observe(viewLifecycleOwner, { dataOrMessage: DataOrMessage<V> -> this.onData(dataOrMessage) })
        return view
    }

    protected open fun onView(view: View) {
        textViewMessage = view.findViewById(R.id.tv_message)
        container = view.findViewById(R.id.container)
    }

    protected fun onData(dataOrMessage: DataOrMessage<V>) {
        when (dataOrMessage.message) {
            DataOrMessage.Message.LOADING -> onMessage(R.string.loading_title)
            DataOrMessage.Message.ERROR -> onMessage(R.string.loading_error)
            else -> onData(dataOrMessage.data)
        }
    }

    protected fun onData(data: V) {
        setDataView(data)
        textViewMessage!!.visibility = View.GONE
        container!!.visibility = View.VISIBLE
    }

    private fun onMessage(resourceId: Int) {
        textViewMessage!!.setText(resourceId)
        textViewMessage!!.visibility = View.VISIBLE
        container!!.visibility = View.GONE
    }
}