package ru.usedesk.chat_sdk.data.repository._extra

import ru.usedesk.common_sdk.entity.exceptions.UsedeskDataNotFoundException

internal abstract class DataLoader<T> {

    private var data: T? = null

    protected abstract fun loadData(): T?

    protected abstract fun saveData(data: T)

    @Throws(UsedeskDataNotFoundException::class)
    fun getData(): T {
        if (data == null) {
            data = loadData()
        }
        return data ?: throw UsedeskDataNotFoundException("Data not found")
    }

    fun setData(data: T?) {
        this.data = data
        if (data != null) {
            saveData(data)
        } else {
            clearData()
        }
    }

    abstract fun clearData()
}