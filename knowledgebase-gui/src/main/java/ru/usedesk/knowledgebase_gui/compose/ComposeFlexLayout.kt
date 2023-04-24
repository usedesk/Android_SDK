
package ru.usedesk.knowledgebase_gui.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import kotlin.math.max


@Composable
internal fun flexMeasurePolicy(
    verticalInterval: Dp,
    horizontalInterval: Dp
): MeasurePolicy = remember(
    key1 = verticalInterval,
    key2 = horizontalInterval
) {
    MeasurePolicy { measurables, constraints ->
        val verticalPx = verticalInterval.roundToPx()
        val horizontalPx = horizontalInterval.roundToPx()
        val flexRows = getFlexRows(
            measurables,
            constraints,
            horizontalPx
        )

        var width = constraints.minWidth
        var height = constraints.minHeight

        if (flexRows.isNotEmpty()) {
            width = flexRows.maxOf { row ->
                row.sumOf(Placeable::width) + max(0, row.size - 1) * horizontalPx
            }
            height = flexRows.sumOf { row ->
                row.maxOf(Placeable::height)
            } + max(0, flexRows.size - 1) * verticalPx
        }

        layout(width, height) {
            var dy = 0
            flexRows.forEach { row ->
                var dx = 0
                row.forEach { placeable ->
                    placeable.placeRelative(dx, dy)
                    dx += placeable.width + horizontalPx
                }
                dy += row.maxOf(Placeable::height) + verticalPx
            }
        }
    }
}

private fun getFlexRows(
    measurables: List<Measurable>,
    constraints: Constraints,
    horizontalInterval: Int
): List<List<Placeable>> {
    val rows = mutableListOf<List<Placeable>>()

    if (measurables.size <= 1) {
        return rows
    }

    var row = mutableListOf<Placeable>()
    var rowWidth = constraints.maxWidth

    rows.add(row)

    measurables.forEach { measurable ->
        val placeable = measurable.measure(constraints)

        when {
            placeable.width + horizontalInterval * row.size <= rowWidth -> {
                row.add(placeable)
                rowWidth -= placeable.width
            }
            row.isEmpty() -> {
                row.add(placeable)
                row = mutableListOf()
                rows.add(row)
                rowWidth = constraints.maxWidth
            }
            else -> {
                row = mutableListOf()
                rows.add(row)
                row.add(placeable)
                rowWidth = constraints.maxWidth - placeable.width
            }
        }
    }

    return rows.filter(List<Placeable>::isNotEmpty)
}