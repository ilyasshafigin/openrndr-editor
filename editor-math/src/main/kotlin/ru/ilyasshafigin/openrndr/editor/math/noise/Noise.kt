package ru.ilyasshafigin.openrndr.editor.math.noise

import ru.ilyasshafigin.openrndr.editor.math.DEG_TO_RAD
import org.openrndr.extra.noise.Random
import kotlin.math.cos
import kotlin.random.Random as DefaultRandom

object Noise {

    private const val PERLIN_YWRAPB = 4
    private const val PERLIN_YWRAP = 1 shl PERLIN_YWRAPB
    private const val PERLIN_ZWRAPB = 8
    private const val PERLIN_ZWRAP = 1 shl PERLIN_ZWRAPB
    private const val PERLIN_SIZE = 4095
    private const val SINCOS_PRECISION = 0.5
    private const val SINCOS_LENGTH = (360 / SINCOS_PRECISION).toInt()

    private var octaves = 4 // default to medium smooth
    private var ampFalloff = 0.5 // 50% reduction/octave
    private val TWOPI = SINCOS_LENGTH
    private val PI = SINCOS_LENGTH shr 1
    private val cosTable: DoubleArray = DoubleArray(TWOPI)
    private val perlin: DoubleArray = DoubleArray(PERLIN_SIZE + 1)

    private var rnd: DefaultRandom
    private var seedTracking: Int = 0

    var seed: String = "OPENRNDR"
        set(value) {
            field = value
            rnd = newRandomGenerator(value)
            initPerlin()
        }

    init {
        for (i in 0 until SINCOS_LENGTH) {
            cosTable[i] = cos(i * DEG_TO_RAD * SINCOS_PRECISION)
        }

        rnd = newRandomGenerator(seed)
        initPerlin()
    }

    fun resetState() {
        rnd = newRandomGenerator(seed)
        initPerlin()
    }

    fun randomizeSeed() {
        val seedBase = seed.replace(Regex("""-\d+"""), "")
        seedTracking = Random.int0(999999)
        seed = "${seedBase}-${seedTracking}"
    }

    fun noise(x: Double): Double {
        return noise(x, 0.0, 0.0)
    }

    fun noise(x: Double, y: Double): Double {
        return noise(x, y, 0.0)
    }

    fun noise(x: Double, y: Double, z: Double): Double {
        var x0 = x
        var y0 = y
        var z0 = z

        if (x0 < 0) x0 = -x0
        if (y0 < 0) y0 = -y0
        if (z0 < 0) z0 = -z0

        var xi = x0.toInt()
        var yi = y0.toInt()
        var zi = z0.toInt()

        var xf = x0 - xi
        var yf = y0 - yi
        var zf = z0 - zi

        var rxf: Double
        var ryf: Double
        var r = 0.0
        var ampl = 0.5
        var n1: Double
        var n2: Double
        var n3: Double

        for (i in 0 until octaves) {
            var of = xi + (yi shl PERLIN_YWRAPB) + (zi shl PERLIN_ZWRAPB)
            rxf = noise_fsc(xf)
            ryf = noise_fsc(yf)

            n1 = perlin[of and PERLIN_SIZE]
            n1 += rxf * (perlin[of + 1 and PERLIN_SIZE] - n1)
            n2 = perlin[of + PERLIN_YWRAP and PERLIN_SIZE]
            n2 += rxf * (perlin[of + PERLIN_YWRAP + 1 and PERLIN_SIZE] - n2)
            n1 += ryf * (n2 - n1)
            of += PERLIN_ZWRAP
            n2 = perlin[of and PERLIN_SIZE]
            n2 += rxf * (perlin[of + 1 and PERLIN_SIZE] - n2)
            n3 = perlin[of + PERLIN_YWRAP and PERLIN_SIZE]
            n3 += rxf * (perlin[of + PERLIN_YWRAP + 1 and PERLIN_SIZE] - n3)
            n2 += ryf * (n3 - n2)
            n1 += noise_fsc(zf) * (n2 - n1)
            r += n1 * ampl
            ampl *= ampFalloff

            xi = xi shl 1
            xf *= 2.0

            yi = yi shl 1
            yf *= 2.0

            zi = zi shl 1
            zf *= 2.0

            if (xf >= 1.0) {
                xi++
                xf--
            }
            if (yf >= 1.0) {
                yi++
                yf--
            }
            if (zf >= 1.0) {
                zi++
                zf--
            }
        }
        return r
    }

    fun noiseDetail(lod: Int) {
        if (lod > 0) {
            octaves = lod
        }
    }

    fun noiseDetail(lod: Int, falloff: Double) {
        if (lod > 0) {
            octaves = lod
        }
        if (falloff > 0) {
            ampFalloff = falloff
        }
    }

    private fun newRandomGenerator(newSeed: String): DefaultRandom {
        return DefaultRandom(stringToInt(newSeed))
    }

    private fun stringToInt(str: String): Int = str.toCharArray().fold(0) { i: Int, c: Char ->
        i + c.toInt()
    }

    private fun noise_fsc(i: Double): Double {
        return (0.5 * (1.0 - cosTable[(i * PI).toInt() % TWOPI.toInt()]))
    }

    private fun initPerlin() {
        for (i in 0 until PERLIN_SIZE + 1) {
            perlin[i] = rnd.nextDouble()
        }
    }
}
