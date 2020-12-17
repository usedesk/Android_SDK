package ru.usedesk.knowledgebase_gui.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.LiveData
import ru.usedesk.common_gui.UsedeskBinding
import ru.usedesk.common_gui.UsedeskFragment
import ru.usedesk.common_gui.inflateItem
import ru.usedesk.knowledgebase_gui.R
import ru.usedesk.knowledgebase_gui.entity.DataOrMessage

abstract class FragmentDataView<DATA, BINDING : UsedeskBinding>(
        private val layoutId: Int
) : UsedeskFragment() {

    private lateinit var textViewMessage: TextView
    private lateinit var binding: BINDING
    private lateinit var container: View

    protected abstract fun setDataView(data: DATA)

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        binding = inflateItem(inflater, container, layoutId) {
            createBinding(it)
        }

        onView(binding.rootView)

        init()

        getLiveData().observe(viewLifecycleOwner, {
            this.onData(it)
        })

        return binding.rootView
    }

    abstract fun createBinding(rootView: View): BINDING

    open fun init() {}

    abstract fun getLiveData(): LiveData<DataOrMessage<DATA>>

    protected open fun onView(view: View) {
        textViewMessage = view.findViewById(R.id.tv_message)
        container = view.findViewById(R.id.container)
    }

    private fun onData(dataOrMessage: DataOrMessage<DATA>) {
        when (dataOrMessage.message) {
            DataOrMessage.Message.LOADING -> onMessage(R.string.loading_title)
            DataOrMessage.Message.ERROR -> onMessage(R.string.loading_error)
            else -> onData(dataOrMessage.data)
        }
    }

    private fun onData(data: DATA?) {
        if (data != null) {
            setDataView(data)
            textViewMessage.visibility = View.GONE
            container.visibility = View.VISIBLE
        }
    }

    private fun onMessage(resourceId: Int) {
        textViewMessage.setText(resourceId)
        textViewMessage.visibility = View.VISIBLE
        container.visibility = View.GONE
    }
}