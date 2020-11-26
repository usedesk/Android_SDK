package ru.usedesk.common_gui.external

public object UsedeskStyleManager {
    private val styleMap = hashMapOf<Int, Int>()

    @JvmStatic
    fun getStyle(defaultStyleId: Int): Int {
        return styleMap[defaultStyleId] ?: defaultStyleId
    }

    @JvmStatic
    fun replaceStyle(defaultStyleId: Int, customStyleId: Int) {
        styleMap[defaultStyleId] = customStyleId
    }
}