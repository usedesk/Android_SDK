
package ru.usedesk.chat_sdk.data.repository._extra

internal abstract class DataLoader<T> {

    private var data: T? = null

    protected abstract fun loadData(): T?

    protected abstract fun saveData(data: T)

    fun getData(): T? = data ?: loadData().also { data = it }

    fun setData(data: T?) {
        this.data = data
        when {
            data != null -> saveData(data)
            else -> clearData()
        }
    }

    abstract fun clearData()
}