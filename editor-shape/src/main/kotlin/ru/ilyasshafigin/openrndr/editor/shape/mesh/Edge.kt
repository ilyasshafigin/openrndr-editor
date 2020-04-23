package ru.ilyasshafigin.openrndr.editor.shape.mesh

import org.openrndr.math.Vector2

class Edge(
    val p1: Vector2,
    val p2: Vector2
) {

    companion object {

        val ZERO = Edge(Vector2.ZERO, Vector2.ZERO)
    }
}
