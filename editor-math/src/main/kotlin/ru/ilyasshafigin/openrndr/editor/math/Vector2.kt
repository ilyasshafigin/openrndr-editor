package ru.ilyasshafigin.openrndr.editor.math

import org.openrndr.math.Vector2
import org.openrndr.math.asDegrees
import org.openrndr.math.asRadians
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

val Vector2.heading: Double
    get() = atan2(y, x).asDegrees

fun Vector2.limit(maxLength: Double): Vector2 {
    val length = length
    return if (length > maxLength && length > EPSILON) {
        this * maxLength / length
    } else {
        copy()
    }
}

fun Vector2.Companion.fromAngle(theta: Double): Vector2 {
    return Vector2(cos(theta.asRadians), sin(theta.asRadians))
}

fun Vector2.coerceAtLeast(min: Double, minY: Double = min): Vector2 {
    return Vector2(x.coerceAtLeast(min), y.coerceAtLeast(minY))
}

fun Vector2.coerceAtMost(max: Double, maxY: Double = max): Vector2 {
    return Vector2(x.coerceAtMost(max), y.coerceAtMost(maxY))
}

fun Vector2.coerceIn(min: Double, max: Double, minY: Double = min, maxY: Double = max): Vector2 {
    require(min > max) { "Cannot coerce value to an empty range: maximum $max is less than minimum $min." }
    return Vector2(x.coerceIn(min, max), y.coerceIn(minY, maxY))
}
