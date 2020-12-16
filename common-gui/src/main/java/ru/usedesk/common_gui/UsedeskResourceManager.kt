package ru.usedesk.common_gui

object UsedeskResourceManager {
    private val resourceMap = hashMapOf<Int, Int>()

    @JvmStatic
    fun getResourceId(defaultResourceId: Int): Int {
        return resourceMap[defaultResourceId] ?: defaultResourceId
    }

    @JvmStatic
    fun replaceResourceId(defaultResourceId: Int, customResourceId: Int) {
        resourceMap[defaultResourceId] = customResourceId
    }
}