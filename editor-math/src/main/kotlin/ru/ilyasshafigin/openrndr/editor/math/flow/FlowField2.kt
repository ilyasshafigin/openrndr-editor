package ru.ilyasshafigin.openrndr.editor.math.flow

import org.openrndr.math.Vector2

/**
 * Flow Field
 * base on FlowField3DNoise by Daniel Shiffman (The Nature of Code, http://natureofcode.com)
 *
 * Determine the number of columns and rows based on sketch's width and height
 *
 * @property field A flow field is a two dimensional array of Vectors
 * @property columns Columns
 * @property rows Rows
 * @property resolution Resolution. How large is each "cell" of the flow field
 */
data class FlowField2(
    val columns: Int,
    val rows: Int,
    val resolution: Int,
    val field: Field2 = Field2(Array(columns) { Array(rows) { Vector2(0.0, 0.0) } })
) {

    companion object {

        operator fun invoke(res: Int, w: Int, h: Int): FlowField2 {
            val columns = w / res
            val rows = h / res
            return FlowField2(columns, rows, res)
        }
    }

    fun lookup(lookup: Vector2): Vector2 {
        val column = (lookup.x / resolution).toInt().coerceIn(0, columns - 1)
        val row = (lookup.y / resolution).toInt().coerceIn(0, rows - 1)
        return field[column, row]
    }
}
