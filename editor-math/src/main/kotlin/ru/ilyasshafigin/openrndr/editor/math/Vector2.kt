package ru.ilyasshafigin.openrndr.editor.math

import org.openrndr.math.Vector2
import org.openrndr.math.mix
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Calculates distance to [other]
 */
infix fun Vector2.distanceTo(other: Vector2): Double {
    val dx = x - other.x
    val dy = y - other.y
    return sqrt(dx * dx + dy * dy)
}

/**
 * Rotates the current vector by [theta] degrees
 * @param theta angle in degrees
 */
infix fun Vector2.rotate(theta: Double): Vector2 {
    val sin = sin(theta.radians)
    val cos = cos(theta.radians)
    return Vector2(
        x * cos - y * sin,
        x * sin + y * cos
    )
}

val Vector2.handing: Double
    get() = atan2(y, x).degrees

fun Vector2.limit(maxLength: Double): Vector2 {
    val length = length
    return if (length > maxLength && length > EPSILON) {
        this * maxLength / length
    } else {
        copy()
    }
}

fun Vector2.Companion.fromAngle(theta: Double): Vector2 {
    return Vector2(cos(theta.radians), sin(theta.radians))
}

fun Vector2.Companion.mix(left: Vector2, right: Vector2, x: Double): Vector2 {
    return Vector2(mix(left.x, right.x, x), mix(left.y, right.y, x))
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
