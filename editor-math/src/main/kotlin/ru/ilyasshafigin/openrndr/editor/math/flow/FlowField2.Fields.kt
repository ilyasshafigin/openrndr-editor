package ru.ilyasshafigin.openrndr.editor.math.flow

import org.openrndr.extra.noise.Random
import org.openrndr.math.Vector2
import org.openrndr.math.asDegrees
import org.openrndr.math.map
import ru.ilyasshafigin.openrndr.editor.image.Image
import ru.ilyasshafigin.openrndr.editor.math.fromAngle
import ru.ilyasshafigin.openrndr.editor.math.heading
import kotlin.math.atan2
import kotlin.math.sqrt

fun FlowField2.updateNoise(maxZOffset: Double) {
    val zoff = Random.double0(maxZOffset)
    var xoff = 0.0
    for (i in 0 until columns) {
        var yoff = 0.0
        for (j in 0 until rows) {
            val p = Random.perlin(xoff, yoff, zoff, Random.Noise.QUINTIC)
            val theta = map(p, -1.0, 1.0, 0.0, 360.0)
            // Make a vector from an angle
            field[i, j] = Vector2.fromAngle(theta)
            yoff += 1.0 / (resolution * 10)
        }
        xoff += 1.0 / (resolution * 10)
    }
}

fun FlowField2.updateFromMouse(x: Double, y: Double, theta: Double, radius: Double) {
    for (i in 0 until columns) {
        val cx = i * resolution
        val dx = cx - x

        for (j in 0 until rows) {
            val cy = j * resolution
            val dy = cy - y

            val distSquare = dx * dx + dy * dy
            if (distSquare > radius * radius) continue

            var angle = field[i, j].heading
            angle += (theta - angle) / sqrt(distSquare / 4)
            field[i, j] = Vector2.fromAngle(angle)
        }
    }
}

fun FlowField2.updateSpiral(center: Vector2) {
    for (i in 0 until columns) {
        for (j in 0 until rows) {
            val x = i * resolution
            val y = j * resolution
            val dx = center.x - x
            val dy = center.y - y
            val theta = atan2(dy, dx).asDegrees
            field[i, j] = Vector2.fromAngle(theta + 60.0) // PI/2 -> circle
        }
    }
}

fun FlowField2.updateHue(source: Image) {
    for (i in 0 until columns) {
        for (j in 0 until rows) {
            val x = i * resolution
            val y = j * resolution
            val pix = source.get(x, y)
            val hue = pix.toHSVa().h
            field[i, j] = Vector2.fromAngle(hue)
        }
    }
}

fun FlowField2.updateBrightness(img: Image) {
    for (i in 0 until columns) {
        for (j in 0 until rows) {
            val x = i * resolution
            val y = j * resolution
            val pix = img.get(x, y)
            val brightness = pix.toHSVa().v
            val theta = map(brightness, 0.0, 1.0, 0.0, 360.0)
            field[i, j] = Vector2.fromAngle(theta)
        }
    }
}

fun FlowField2.invert() {
    for (i in 0 until columns) {
        for (j in 0 until rows) {
            field[i, j] = field[i, j].times(-1.0)
        }
    }
}
