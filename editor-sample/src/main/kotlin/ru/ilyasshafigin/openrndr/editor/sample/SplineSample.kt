package ru.ilyasshafigin.openrndr.editor.sample

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.math.Vector2
import ru.ilyasshafigin.openrndr.editor.shape.spline

fun main() = application {
    program {
        extend {
            with(drawer) {
                clear(ColorRGBa.WHITE)
                fill = null
                stroke = ColorRGBa.BLACK
                strokeWeight = 2.0

                val paddingX = width / 10.0
                val paddingY = height / 10.0
                val left = paddingX
                val right = width - paddingX
                val top = paddingY
                val bottom = height - paddingY

                val spline = spline(detail = 3) {
                    vertex(left, top)
                    vertex(left, top)
                    vertex(left + 50.0, top + 10.0)
                    vertex(left + 100.0, top)
                    vertex(left + 150.0, top - 10.0)
                    vertex(left + 200.0, top)
                    vertex(left + 200.0, top)
                }
                val points = spline.adaptivePositions(0.0)

                spline.segments.forEachIndexed { index, segment ->
                    stroke = if (index % 2 == 0) ColorRGBa.BLACK else ColorRGBa.GRAY
                    segment(segment)
                }

                stroke = ColorRGBa.GREEN
                points.forEach { point ->
                    circle(point, 2.0)
                }

                stroke = ColorRGBa.RED
                strokeWeight = 1.0
                lineSegment(Vector2(left, top), Vector2(left, bottom))
                lineSegment(Vector2(right, top), Vector2(right, bottom))
            }
        }
    }
}
