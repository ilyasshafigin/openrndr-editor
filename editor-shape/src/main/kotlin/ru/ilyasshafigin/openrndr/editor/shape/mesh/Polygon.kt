package ru.ilyasshafigin.openrndr.editor.shape.mesh

import org.openrndr.math.Vector2

class Polygon(val points: List<Vector2>) {

    val center: Vector2
        get() {
            if (points.isEmpty()) return Vector2.ZERO
            if (points.size == 1) return points[0]

            var cx = points[0].x
            var cy = points[0].y

            for (i in 1 until points.size) {
                val p = points[i]
                cx += p.x
                cy += p.y
            }

            return Vector2(cx, cy) / points.size.toDouble()
        }
}
