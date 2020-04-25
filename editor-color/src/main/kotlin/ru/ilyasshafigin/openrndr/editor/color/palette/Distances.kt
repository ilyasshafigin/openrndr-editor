package ru.ilyasshafigin.openrndr.editor.color.palette

import org.openrndr.color.ColorLABa
import org.openrndr.color.ColorRGBa
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sqrt

enum class PaletteDistance {
    EUCLIDEAN,
    CMC,
    COMPROMISE,
    PROTANOPE,
    DEUTERANOPE,
    TRITANOPE
}

internal enum class ConfusionLineType(val x: Double, val y: Double, val m: Double, val yint: Double) {
    PROTANOPE(0.7465, 0.2535, 1.273463, -0.073894),
    DEUTERANOPE(1.4, -0.4, 0.968437, 0.003331),
    TRITANOPE(0.1748, 0.0, 0.062921, 0.292119)
}

internal typealias PaletteConfusion = (ColorLABa, ColorLABa) -> Double

internal class CachedDistances {

    private fun simulate(lab: ColorLABa, type: ConfusionLineType, amount: Double = 1.0): ColorLABa {
        // Cache
        val key =
            "${(lab.l * 100).toInt()}-${(lab.a * 100).toInt()}-${(lab.b * 100).toInt()}-$type-${(amount * 100).toInt()}"
        val cached = cache[key]
        if (cached != null) return cached

        // Get data from type
        val confuseX = type.x
        val confuseY = type.y
        val confuseM = type.m
        val confuseYint = type.yint

        // Code adapted from http://galacticmilk.com/labs/Color-Vision/Javascript/Color.Vision.Simulate.js
        val color = lab.toRGBa()

        val sr = color.r * 255.0
        val sg = color.g * 255.0
        val sb = color.b * 255.0
        var dr = sr // destination color
        var dg = sg
        var db = sb
        // Convert source color into XYZ color space
        val powR = sr.pow(2.2)
        val powG = sg.pow(2.2)
        val powB = sb.pow(2.2)
        var X = powR * 0.412424 + powG * 0.357579 + powB * 0.180464 // RGB->XYZ (sRGB:D65)
        val Y = powR * 0.212656 + powG * 0.715158 + powB * 0.0721856
        var Z = powR * 0.0193324 + powG * 0.119193 + powB * 0.950444
        // Convert XYZ into xyY Chromacity Coordinates (xy) and Luminance (Y)
        val chromaX = X / (X + Y + Z)
        val chromaY = Y / (X + Y + Z)
        // Generate the "Confusion Line" between the source color and the Confusion Point
        val m = (chromaY - confuseY) / (chromaX - confuseX) // slope of Confusion Line
        val yint = chromaY - chromaX * m // y-intercept of confusion line (x-intercept = 0.0)
        // How far the xy coords deviate from the simulation
        val deviateX = (confuseYint - yint) / (m - confuseM)
        val deviateY = m * deviateX + yint
        // Compute the simulated color's XYZ coords
        X = (deviateX * Y) / deviateY
        Z = ((1.0 - (deviateX + deviateY)) * Y) / deviateY
        // Neutral grey calculated from luminance (in D65)
        val neutralX = (0.312713 * Y) / 0.329016
        val neutralZ = (0.358271 * Y) / 0.329016
        // Difference between simulated color and neutral grey
        val diffX = neutralX - X
        val diffZ = neutralZ - Z
        val diffR = diffX * 3.24071 + diffZ * -0.498571 // XYZ->RGB (sRGB:D65)
        val diffG = diffX * -0.969258 + diffZ * 0.0415557
        val diffB = diffX * 0.0556352 + diffZ * 1.05707
        // Convert to RGB color space
        dr = X * 3.24071 + Y * -1.53726 + Z * -0.498571 // XYZ->RGB (sRGB:D65)
        dg = X * -0.969258 + Y * 1.87599 + Z * 0.0415557
        db = X * 0.0556352 + Y * -0.203996 + Z * 1.05707
        // Compensate simulated color towards a neutral fit in RGB space
        val fitR = ((if (dr < 0.0) 0.0 else 1.0) - dr) / diffR
        val fitG = ((if (dg < 0.0) 0.0 else 1.0) - dg) / diffG
        val fitB = ((if (db < 0.0) 0.0 else 1.0) - db) / diffB
        // highest value
        val adjust = maxOf(
            if (fitR > 1.0 || fitR < 0.0) 0.0 else fitR,
            if (fitG > 1.0 || fitG < 0.0) 0.0 else fitG,
            if (fitB > 1.0 || fitB < 0.0) 0.0 else fitB
        )
        // Shift proportional to the greatest shift
        dr += adjust * diffR
        dg += adjust * diffG
        db += adjust * diffB
        // Apply gamma correction
        dr = dr.pow(1.0 / 2.2)
        dg = dg.pow(1.0 / 2.2)
        db = db.pow(1.0 / 2.2)
        // Anomylize colors
        dr = sr * (1.0 - amount) + dr * amount
        dg = sg * (1.0 - amount) + dg * amount
        db = sb * (1.0 - amount) + db * amount
        val result = ColorRGBa(dr / 255.0, dg / 255.0, db / 255.0).toLABa()
        cache[key] = result
        return result
    }

    private val euclidean: PaletteConfusion = { lab1, lab2 -> euclideanInternal(lab1, lab2) }
    private val protanope: PaletteConfusion = { lab1, lab2 -> colorblind(ConfusionLineType.PROTANOPE, lab1, lab2) }
    private val deuteranope: PaletteConfusion = { lab1, lab2 -> colorblind(ConfusionLineType.DEUTERANOPE, lab1, lab2) }
    private val tritanope: PaletteConfusion = { lab1, lab2 -> colorblind(ConfusionLineType.TRITANOPE, lab1, lab2) }
    private val cmc: PaletteConfusion = { lab1, lab2 -> cmcInternal(lab1, lab2) }
    private val compromise: PaletteConfusion = { lab1, lab2 ->
        var total = 0.0

        var d = cmc(lab1, lab2)
        total += d * 1000.0

        d = protanope(lab1, lab2)
        if (!d.isNaN()) total += d * 100.0

        d = deuteranope(lab1, lab2)
        if (!d.isNaN()) total += d * 500.0

        d = tritanope(lab1, lab2)
        if (!d.isNaN()) total += d * 1.0

        total / COMPROMISE_COUNT
    }

    private fun colorblind(type: ConfusionLineType, lab1: ColorLABa, lab2: ColorLABa): Double {
        return cmcInternal(simulate(lab1, type), simulate(lab2, type))
    }

    fun get(type: PaletteDistance): PaletteConfusion = when (type) {
        PaletteDistance.EUCLIDEAN -> euclidean
        PaletteDistance.CMC -> cmc
        PaletteDistance.COMPROMISE -> compromise
        PaletteDistance.PROTANOPE -> protanope
        PaletteDistance.DEUTERANOPE -> deuteranope
        PaletteDistance.TRITANOPE -> tritanope
    }

    companion object {

        const val COMPROMISE_COUNT = 1000.0 + 100.0 + 500.0 + 1.0

        private val cache = mutableMapOf<String, ColorLABa>()
    }
}

private fun euclideanInternal(lab1: ColorLABa, lab2: ColorLABa): Double {
    return sqrt(
        (lab1.l - lab2.l) * (lab1.l - lab2.l) +
            (lab1.a - lab2.a) * (lab1.a - lab2.a) +
            (lab1.b - lab2.b) * (lab1.b - lab2.b)
    )
}

private fun cmcInternal(lab1: ColorLABa, lab2: ColorLABa, l: Double = 2.0, c: Double = 1.0): Double {
    val l1 = lab1.l
    val l2 = lab2.l
    val a1 = lab1.a
    val a2 = lab2.a
    val b1 = lab1.b
    val b2 = lab2.b
    val c1 = sqrt(a1 * a1 + b1 * b1)
    val c2 = sqrt(a2 * a2 + b2 * b2)
    val deltaC = c1 - c2
    val deltaL = l1 - l2
    val deltaA = a1 - a2
    val deltaB = b1 - b2
    val deltaH = sqrt(deltaA * deltaA + deltaB * deltaB + deltaC * deltaC)
    var h1 = atan2(b1, a1) * (180.0 / Math.PI)
    while (h1 < 0.0) {
        h1 += 360.0
    }
    val f = sqrt(c1.pow(4) / (c1.pow(4) + 1900))
    val t = if (h1 in 164.0..345.0) 0.56 + abs(0.2 * cos(h1 + 168.0)) else 0.36 + abs(0.4 * cos(h1 + 35.0))
    val sl = if (l1 < 16.0) 0.511 else (0.040975 * l1) / (1.0 + 0.01765 * l1)
    val sc = (0.0638 * c1) / (1.0 + 0.0131 * c1) + 0.638
    val sh = sc * (f * t + 1.0 - f)
    return sqrt((deltaL / (l * sl)).pow(2) + (deltaC / (c * sc)).pow(2) + (deltaH / sh).pow(2))
}
