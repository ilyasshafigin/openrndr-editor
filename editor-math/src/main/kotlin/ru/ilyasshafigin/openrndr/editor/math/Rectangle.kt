package ru.ilyasshafigin.openrndr.editor.math

import org.openrndr.math.Vector2
import org.openrndr.shape.Rectangle

fun Rectangle.Companion.fromCorners(topLeft: Vector2, rightBottom: Vector2): Rectangle {
    return Rectangle(topLeft, rightBottom.x - topLeft.x, rightBottom.y - topLeft.y)
}
