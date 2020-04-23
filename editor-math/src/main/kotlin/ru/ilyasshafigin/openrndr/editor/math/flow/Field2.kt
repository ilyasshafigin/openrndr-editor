package ru.ilyasshafigin.openrndr.editor.math.flow

import org.openrndr.math.Vector2

data class Field2(
    val field: Array<Array<Vector2>>
) {

    operator fun get(x: Int, y: Int): Vector2 = field[x][y]

    operator fun set(x: Int, y: Int, value: Vector2) {
        field[x][y] = value
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Field2

        if (!field.contentDeepEquals(other.field)) return false

        return true
    }

    override fun hashCode(): Int {
        return field.contentDeepHashCode()
    }
}
