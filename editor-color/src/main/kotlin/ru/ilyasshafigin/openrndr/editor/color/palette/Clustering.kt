package ru.ilyasshafigin.openrndr.editor.color.palette

import org.openrndr.color.ColorLABa
import org.openrndr.math.Vector3
import kotlin.math.pow
import kotlin.random.Random

/**
 * Algorithms to ensure an even distribution of colors
 */
enum class PaletteClustering {
    /** Force vector repulsion algorithm (fast) */
    FORCE_VECTOR,
    /** K-means algorithm (slow) */
    K_MEANS
}

private const val REPULSION = 100.0
private const val SPEED = 100.0

internal fun forceVector(
    rng: Random,
    distance: PaletteConfusion,
    filter: ColorFilter,
    quality: Int,
    colors: MutableList<ColorLABa>
) {
    val vectors = Array(colors.size) { Vector3.ZERO }
    var steps = quality * 20
    val l = colors.size

    while (steps-- > 0) {
        // Initializing vectors
        for (i in 0 until l) vectors[i] = Vector3.ZERO

        // Computing force
        for (i in 0 until l) {
            val A = colors[i]

            for (j in 0 until i) {
                val B = colors[j]

                // Repulsion
                val d = distance(A, B)

                if (d > 0.0) {
                    val dl = A.l - B.l
                    val da = A.a - B.a
                    val db = A.b - B.b
                    val force = REPULSION / d.pow(2)
                    val dv = Vector3(dl, da, db) * (force / d)

                    vectors[i] += dv
                    vectors[i] -= dv
                } else {
                    // Jitter
                    vectors[j] += Vector3(
                        2.0 - 4.0 * rng.nextDouble(),
                        2.0 - 4.0 * rng.nextDouble(),
                        2.0 - 4.0 * rng.nextDouble()
                    )
                }
            }
        }

        // Applying force
        for (i in 0 until l) {
            val color = colors[i]
            val displacement = SPEED * vectors[i].length
            if (displacement > 0.0) {
                val ratio = (SPEED * 0.1.coerceAtMost(displacement)) / displacement
                val candidateLab = ColorLABa(
                    color.l + vectors[i].x * ratio,
                    color.a + vectors[i].y * ratio,
                    color.b + vectors[i].z * ratio
                )
                val rgb = candidateLab.toRGBa()

                if (filter(rgb, candidateLab)) {
                    colors[i] = candidateLab
                }
            }
        }
    }
}

internal fun kMeans(
    distance: PaletteConfusion,
    filter: ColorFilter,
    ultraPrecision: Boolean,
    quality: Int,
    colors: MutableList<ColorLABa>
) {
    val colorSamples = mutableListOf<ColorLABa>()
    val linc = if (ultraPrecision) 1 else 5
    val ainc = if (ultraPrecision) 5 else 10
    val binc = if (ultraPrecision) 5 else 10

    for (l in 0..100 step linc) {
        for (a in -100..100 step ainc) {
            for (b in -100..100 step binc) {
                val lab = ColorLABa(l.toDouble(), a.toDouble(), b.toDouble())
                val rgb = lab.toRGBa()

                if (!filter(rgb, lab))
                    continue

                colorSamples.add(lab)
            }
        }
    }

    val samplesClosest = IntArray(colorSamples.size) { -1 }

    // Steps
    var steps = quality
    val li = colorSamples.size
    val lj = colors.size

    while (steps-- > 0) {
        // Finding closest color
        for (i in 0 until li) {
            val B = colorSamples[i]
            var minDistance = Double.POSITIVE_INFINITY

            for (j in 0 until lj) {
                val A = colors[j]
                val d = distance(A, B)

                if (d < minDistance) {
                    minDistance = d
                    samplesClosest[i] = j
                }
            }
        }

        var freeColorSamples = colorSamples.toList()

        for (j in 0 until lj) {
            var count = 0
            var candidate = ColorLABa(0.0, 0.0, 0.0)

            for (i in 0 until li) {
                if (samplesClosest[i] == j) {
                    count++
                    candidate = ColorLABa(
                        candidate.l + colorSamples[i].l,
                        candidate.a + colorSamples[i].a,
                        candidate.b + colorSamples[i].b
                    )
                }
            }

            if (count != 0) {
                candidate = ColorLABa(
                    candidate.l / count,
                    candidate.a / count,
                    candidate.b / count
                )

                val rgb = candidate.toRGBa()

                if (filter(rgb, candidate)) {
                    colors[j] = candidate
                } else {
                    // The candidate is out of the boundaries of our color space or unfound
                    if (freeColorSamples.isNotEmpty()) {
                        // We just search for the closest free color
                        var minDistance = Double.POSITIVE_INFINITY
                        var closest = -1

                        for (i in freeColorSamples.indices) {
                            val d = distance(freeColorSamples[i], candidate)
                            if (d < minDistance) {
                                minDistance = d
                                closest = i
                            }
                        }

                        colors[j] = colorSamples[closest]
                    } else {
                        // Then we just search for the closest color
                        var minDistance = Double.POSITIVE_INFINITY
                        var closest = -1

                        for (i in 0 until colorSamples.size) {
                            val d = distance(colorSamples[i], candidate)
                            if (d < minDistance) {
                                minDistance = d
                                closest = i
                            }
                        }

                        colors[j] = colorSamples[closest]
                    }

                    // Cleaning up free samples
                    freeColorSamples = freeColorSamples.filter { color ->
                        color.l != colors[j].l || color.a != colors[j].a || color.b != colors[j].b
                    }
                }
            }
        }
    }
}
