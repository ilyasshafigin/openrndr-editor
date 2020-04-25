package ru.ilyasshafigin.openrndr.editor.color.palette

import org.openrndr.color.ColorLABa
import org.openrndr.color.ColorRGBa
import kotlin.math.sign
import kotlin.random.Random

typealias ColorFilter = (rgb: ColorRGBa, lab: ColorLABa) -> Boolean

fun generatePalette(
    colorCount: Int,
    colorSpace: ColorSpace = ColorSpace.default,
    clustering: PaletteClustering = PaletteClustering.K_MEANS,
    distance: PaletteDistance = PaletteDistance.COMPROMISE,
    quality: Int = 50,
    ultraPrecision: Boolean = false,
    rnd: Random = org.openrndr.extra.noise.Random.rnd
): List<ColorRGBa> = generatePalette(
    colorCount = colorCount,
    colorFilter = colorSpace.toColorFilter(),
    clustering = clustering,
    quality = quality,
    ultraPrecision = ultraPrecision,
    distance = distance,
    rnd = rnd
)

/**
 * Function generating a palette.
 *
 * Adapted from https://github.com/medialab/iwanthue
 *
 * @param colorCount number of colors in the generated palette
 * @param colorFilter function filtering unwanted colors
 * @param clustering clustering method to use
 * @param distance distance function to use
 * @param quality quality of the clustering: iterations factor for force-vector, colorspace sampling for k-means.
 * @param ultraPrecision ultra precision for k-means colorspace sampling
 * @return the computed palette as an list of [ColorRGBa]
 */
fun generatePalette(
    colorCount: Int,
    colorFilter: ColorFilter,
    clustering: PaletteClustering = PaletteClustering.K_MEANS,
    distance: PaletteDistance = PaletteDistance.EUCLIDEAN,
    quality: Int = 50,
    ultraPrecision: Boolean = false,
    rnd: Random = org.openrndr.extra.noise.Random.rnd
): List<ColorRGBa> {
    require(colorCount > 1) { "Invalid `colorCount`. Expecting a number > 2" }
    require(quality > 0) { "Invalid `quality`. Expecting a number > 0" }

    val distances = CachedDistances()
    val distanceFunc = distances.get(distance)

    val colors = sampleLabColors(rnd, colorCount, colorFilter)

    when (clustering) {
        PaletteClustering.FORCE_VECTOR -> forceVector(rnd, distanceFunc, colorFilter, quality, colors)
        PaletteClustering.K_MEANS -> kMeans(distanceFunc, colorFilter, ultraPrecision, quality, colors)
    }

    colors.sortWith(Comparator { a, b -> distanceFunc(a, b).sign.toInt() })
    return colors.map(ColorLABa::toRGBa)
}

private fun sampleLabColors(rnd: Random, count: Int, colorFilter: ColorFilter): MutableList<ColorLABa> {
    val colors = mutableListOf<ColorLABa>()
    var lab: ColorLABa
    var rgb: ColorRGBa

    for (i in 0 until count) {
        do {
            lab = ColorLABa(
                100.0 * rnd.nextDouble(),
                100.0 * (2.0 * rnd.nextDouble() - 1.0),
                100.0 * (2.0 * rnd.nextDouble() - 1.0)
            )
            rgb = lab.toRGBa()
        } while (!colorFilter(rgb, lab))

        colors.add(lab)
    }

    return colors
}
