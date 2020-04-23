package ru.ilyasshafigin.openrndr.editor.shape.mesh

import org.openrndr.math.Vector2

class Triangle(
    val p1: Vector2,
    val p2: Vector2,
    val p3: Vector2
) {

    fun sharesVertex(other: Triangle): Boolean {
        return p1 === other.p1 || p1 === other.p2 || p1 === other.p3 ||
            p2 === other.p1 || p2 === other.p2 || p2 === other.p3 ||
            p3 === other.p1 || p3 === other.p2 || p3 === other.p3
    }
}
