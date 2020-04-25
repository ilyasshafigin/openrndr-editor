package ru.ilyasshafigin.openrndr.editor.color.palette

/**
 * Color space for filtering. Herein used LCHAB colors.
 *
 * - [hmin] and [hmax] from 0 to 360
 * - [cmin] and [cmax] from 0 to 100
 * - [lmin] and [lmax] from 0 to 100
 */
data class ColorSpace(
    val hmin: Double,
    val hmax: Double,
    val cmin: Double,
    val cmax: Double,
    val lmin: Double,
    val lmax: Double
) {

    companion object Preset {

        val all = ColorSpace(0.0, 360.0, 0.0, 100.0, 0.0, 100.0)
        val default = ColorSpace(0.0, 360.0, 30.0, 80.0, 35.0, 80.0)
        val colorblind = ColorSpace(0.0, 360.0, 40.0, 70.0, 15.0, 85.0)
        val fancyLight = ColorSpace(0.0, 360.0, 15.0, 40.0, 70.0, 100.0)
        val fancyDark = ColorSpace(0.0, 360.0, 8.0, 40.0, 7.0, 40.0)
        val shades = ColorSpace(0.0, 240.0, 0.0, 15.0, 0.0, 100.0)
        val tarnish = ColorSpace(0.0, 360.0, 0.0, 15.0, 30.0, 70.0)
        val pastel = ColorSpace(0.0, 360.0, 0.0, 30.0, 70.0, 100.0)
        val pimp = ColorSpace(0.0, 360.0, 30.0, 100.0, 25.0, 70.0)
        val intense = ColorSpace(0.0, 360.0, 20.0, 100.0, 15.0, 80.0)
        val fluo = ColorSpace(0.0, 300.0, 35.0, 100.0, 75.0, 100.0)
        val redRoses = ColorSpace(330.0, 20.0, 10.0, 100.0, 35.0, 100.0)
        val ochreSand = ColorSpace(20.0, 60.0, 20.0, 50.0, 35.0, 100.0)
        val yellowLime = ColorSpace(60.0, 90.0, 10.0, 100.0, 35.0, 100.0)
        val greenMint = ColorSpace(90.0, 150.0, 10.0, 100.0, 35.0, 100.0)
        val iceCube = ColorSpace(150.0, 200.0, 0.0, 100.0, 35.0, 100.0)
        val blueOcean = ColorSpace(220.0, 260.0, 8.0, 80.0, 0.0, 50.0)
        val indigoNight = ColorSpace(260.0, 290.0, 40.0, 100.0, 35.0, 100.0)
        val purpleWine = ColorSpace(290.0, 330.0, 0.0, 100.0, 0.0, 40.0)
    }
}

fun ColorSpace.toColorFilter(): ColorFilter = if (hmin < hmax) {
    { _, lab ->
        val lch = lab.toLCHABa()
        lch.h in hmin..hmax && lch.c in cmin..cmax && lch.l in lmin..lmax
    }
} else {
    { _, lab ->
        val lch = lab.toLCHABa()
        (lch.h >= hmin || lch.h <= hmax) && lch.c in cmin..cmax && lch.l in lmin..lmax
    }
}
