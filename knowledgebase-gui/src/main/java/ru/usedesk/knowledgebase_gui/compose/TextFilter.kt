
package ru.usedesk.knowledgebase_gui.compose

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import kotlin.math.max

internal abstract class TextFilter(private val maxLength: Int) {
    abstract fun filterChar(char: Char): Boolean

    fun onValueChanged(newValue: TextFieldValue): TextFieldValue {
        val filteredText = getFilteredString(newValue.text)
        val filtered = newValue.text.length - filteredText.length
        val selection = when (filtered) {
            0 -> newValue.selection
            else -> TextRange(max(0, newValue.selection.start - filtered))
        }
        val filteredMaxText = getMaxString(filteredText)

        return TextFieldValue(
            filteredMaxText,
            selection,
            newValue.composition
        )
    }

    fun getFilteredString(text: String) = text.filter(this::filterChar)

    fun getMaxString(text: String): String = text.take(maxLength)

    class SingleLine(maxLength: Int = 64) : TextFilter(maxLength) {
        override fun filterChar(char: Char) = char !in "\n\r\t"
    }

    class MultiLine(maxLength: Int = 64) : TextFilter(maxLength) {
        override fun filterChar(char: Char) = true
    }

    /*open class Number(maxLength: Int = 64) : TextFilter(maxLength) {
        override fun filterChar(char: Char) = char.isDigit()
    }

    class None(maxLength: Int = 64) : TextFilter(maxLength) {
        override fun filterChar(char: Char) = true
    }

    class Phone(maxLength: Int = 13) : Number(maxLength)

    class Email(maxLength: Int = 64) : TextFilter(maxLength) {
        override fun filterChar(char: Char) = char.lowercaseChar() in 'a'..'z' ||
                char in '0'..'9' ||
                char in "@._-+"
    }*/
}