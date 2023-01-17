package ru.ilyasshafigin.openrndr.editor.color

import org.openrndr.math.Vector3
import org.openrndr.math.max
import org.openrndr.math.min

// Spectral Colour Schemes
// By Alan Zucconi
// Website: www.alanzucconi.com
// Twitter: @AlanZucconi

// Read "Improving the Rainbow" for more information
// http://www.alanzucconi.com/?p=6703

fun spectralZucconi6(x: Double): Vector3 {
    return bump3y(c1 * (Vector3(x, x, x) - x1), y1) + bump3y(c2 * (Vector3(x, x, x)  - x2), y2)
}

fun saturate(x: Vector3): Vector3 {
    return min(Vector3.ONE, max(Vector3.ZERO, x))
}

fun bump3y(x: Vector3, yoffset: Vector3): Vector3 {
    var y: Vector3 = Vector3.ONE - x * x
    y = saturate(y - yoffset)
    return y
}

private val c1 = Vector3(3.54585104, 2.93225262, 2.41593945)
private val x1 = Vector3(0.69549072, 0.49228336, 0.27699880)
private val y1 = Vector3(0.02312639, 0.15225084, 0.52607955)

private val c2 = Vector3(3.90307140, 3.21182957, 3.96587128)
private val x2 = Vector3(0.11748627, 0.86755042, 0.66077860)
private val y2 = Vector3(0.84897130, 0.88445281, 0.73949448)
