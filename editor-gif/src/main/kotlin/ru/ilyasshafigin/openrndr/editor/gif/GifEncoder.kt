package ru.ilyasshafigin.openrndr.editor.gif

import org.openrndr.color.ColorRGBa
import java.io.IOException
import java.io.OutputStream

/**
 * Class AnimatedGifEncoder - Encodes a GIF file consisting of one or more
 * frames.
 *
 * No copyright asserted on the source code of this class. May be used for any
 * purpose, however, refer to the Unisys LZW patent for restrictions on use of
 * the associated LZWEncoder class. Please forward any corrections to
 * kweiner@fmsware.com.
 *
 * @author Kevin Weiner, FM Software
 * @version 1.03 November 2003
 */
class GifEncoder(
    private val width: Int,
    private val height: Int,
    private val transparentColor: ColorRGBa?,
    private val repeat: Int = -1,
    private val delay: Int = 0,
    private val dispose: Int = -1,
    private val sample: Int = 10
) {

    // ready to output frames
    private var started: Boolean = false
    // number of bit planes
    private val colorDepth: Int = 8
    // active palette entries
    private val usedEntry = BooleanArray(256)
    // color table size (bits-1)
    private val palSize: Int = 7

    /**
     * Adds next GIF frame. The frame is not written immediately, but is actually
     * deferred until the next frame is received so that timing data can be
     * inserted. Invoking `stop()` flushes all frames.
     *
     * @return `true` if successful.
     */
    fun write(outs: OutputStream, pixels: ByteArray, isFirstFrame: Boolean): Boolean {
        if (!started) {
            return false
        }
        try {
            // converted frame indexed to palette
            val indexedPixels = ByteArray(pixels.size / 3)
            // build color table & map pixels
            val colorTab = analyzePixels(pixels, indexedPixels, usedEntry, sample)
            // get closest match to transparent color if specified
            val transIndex = transparentColor?.let { backgroundColor ->
                colorTab.findClosest(backgroundColor, usedEntry)
            } ?: 0
            if (isFirstFrame) {
                // logical screen descriptior
                outs.writeLSD(width, height, palSize)
                // global color table
                outs.writePalette(colorTab)
                if (repeat >= 0) {
                    // use NS app extension to indicate reps
                    outs.writeNetscapeExt(repeat)
                }
            }
            // write graphic control extension
            outs.writeGraphicCtrlExt(transparentColor, dispose, delay, transIndex)
            // image descriptor
            outs.writeImageDesc(width, height, isFirstFrame, palSize)
            if (!isFirstFrame) {
                // local color table
                outs.writePalette(colorTab)
            }
            // encode and write pixel data
            outs.writePixels(width, height, indexedPixels, colorDepth)
            return true
        } catch (exception: IOException) {
            return false
        }
    }

    /**
     * Flushes any pending data and closes output file. If writing to an
     * OutputStream, the stream is not closed.
     * @return `true` if data output closed
     */
    fun stop(outs: OutputStream): Boolean {
        if (!started) return false
        started = false
        return try {
            outs.write(0x3b) // gif trailer
            outs.flush()
            true
        } catch (exception: IOException) {
            false
        }
    }

    /**
     * Initiates GIF file creation on the given stream. The stream is not closed
     * automatically.
     *
     * @param outs on which GIF images are written.
     * @return `false` if initial write failed.
     */
    fun start(outs: OutputStream): Boolean {
        var ok = true
        try {
            outs.writeString("GIF89a") // header
        } catch (exception: IOException) {
            ok = false
        }
        started = ok
        return ok
    }

    /**
     * Analyzes image colors and creates color map.
     */
    private fun analyzePixels(
        pixels: ByteArray,
        indexedPixels: ByteArray,
        usedEntry: BooleanArray,
        sample: Int
    ): ByteArray {
        val len = pixels.size
        val nPix = len / 3
        val nq = NeuQuant(pixels, len, sample)
        // initialize quantizer
        val colorTab = nq.process() // create reduced palette
        // convert map from BGR to RGB
        run {
            var i = 0
            while (i < colorTab.size) {
                val temp = colorTab[i]
                colorTab[i] = colorTab[i + 2]
                colorTab[i + 2] = temp
                usedEntry[i / 3] = false
                i += 3
            }
        }
        // map image pixels to new palette
        var k = 0
        for (i in 0 until nPix) {
            val index = nq.map(
                pixels[k++].toInt() and 0xff,
                pixels[k++].toInt() and 0xff,
                pixels[k++].toInt() and 0xff
            )
            usedEntry[index] = true
            indexedPixels[i] = index.toByte()
        }
        return colorTab
    }

    /**
     * Returns index of palette color closest to c
     *
     */
    private fun ByteArray.findClosest(c: ColorRGBa, usedEntry: BooleanArray): Int {
        val r = (c.r * 255).toInt()
        val g = (c.g * 255).toInt()
        val b = (c.b * 255).toInt()
        var minpos = 0
        var dmin = 256 * 256 * 256
        val len = size
        var i = 0
        while (i < len) {
            val dr = r - (get(i++).toInt() and 0xff)
            val dg = g - (get(i++).toInt() and 0xff)
            val db = b - (get(i).toInt() and 0xff)
            val d = dr * dr + dg * dg + db * db
            val index = i / 3
            if (usedEntry[index] && d < dmin) {
                dmin = d
                minpos = index
            }
            i++
        }
        return minpos
    }

    /**
     * Writes Graphic Control Extension
     */
    @Throws(IOException::class)
    private fun OutputStream.writeGraphicCtrlExt(transparent: ColorRGBa?, dispose: Int, delay: Int, transIndex: Int) {
        write(0x21) // extension introducer
        write(0xf9) // GCE label
        write(4) // data block size
        val transp: Int
        var disp: Int
        if (transparent == null) {
            transp = 0
            disp = 0 // dispose = no action
        } else {
            transp = 1
            disp = 2 // force clear if using transparent color
        }
        if (dispose >= 0) {
            disp = dispose and 7 // user override
        }
        disp = disp shl 2
        // packed fields
        write(
            0 or  // 1:3 reserved
                disp or  // 4:6 disposal
                0 or  // 7 user input - 0 = none
                transp
        ) // 8 transparency flag
        writeShort(delay) // delay x 1/100 sec
        write(transIndex) // transparent color index
        write(0) // block terminator
    }

    /**
     * Writes Image Descriptor
     */
    @Throws(IOException::class)
    private fun OutputStream.writeImageDesc(width: Int, height: Int, firstFrame: Boolean, palSize: Int) {
        write(0x2c) // image separator
        writeShort(0) // image position x,y = 0,0
        writeShort(0)
        writeShort(width) // image size
        writeShort(height)
        // packed fields
        if (firstFrame) { // no LCT - GCT is used for first (or only) frame
            write(0)
        } else { // specify normal LCT
            write(
                0x80 or  // 1 local color table 1=yes
                    0 or  // 2 interlace - 0=no
                    0 or  // 3 sorted - 0=no
                    0 or  // 4-5 reserved
                    palSize
            ) // 6-8 size of color table
        }
    }

    /**
     * Writes Logical Screen Descriptor
     */
    @Throws(IOException::class)
    private fun OutputStream.writeLSD(width: Int, height: Int, palSize: Int) {
        // logical screen size
        writeShort(width)
        writeShort(height)
        // packed fields
        write(
            0x80 or  // 1 : global color table flag = 1 (gct used)
                0x70 or  // 2-4 : color resolution = 7
                0x00 or  // 5 : gct sort flag = 0
                palSize
        ) // 6-8 : gct size
        write(0) // background color index
        write(0) // pixel aspect ratio - assume 1:1
    }

    /**
     * Writes Netscape application extension to define repeat count.
     */
    @Throws(IOException::class)
    private fun OutputStream.writeNetscapeExt(repeat: Int) {
        write(0x21) // extension introducer
        write(0xff) // app extension label
        write(11) // block size
        writeString("NETSCAPE" + "2.0") // app id + auth code
        write(3) // sub-block size
        write(1) // loop sub-block id
        writeShort(repeat) // loop count (extra iterations, 0=repeat forever)
        write(0) // block terminator
    }

    /**
     * Writes color table
     */
    @Throws(IOException::class)
    private fun OutputStream.writePalette(colorTab: ByteArray) {
        write(colorTab, 0, colorTab.size)
        val n = 3 * 256 - colorTab.size
        for (i in 0 until n) {
            write(0)
        }
    }

    /**
     * Encodes and writes pixel data
     */
    @Throws(IOException::class)
    private fun OutputStream.writePixels(width: Int, height: Int, indexedPixels: ByteArray, colorDepth: Int) {
        val encoder = LZWEncoder(width, height, indexedPixels, colorDepth)
        encoder.encode(this)
    }

    /**
     * Write 16-bit value to output stream, LSB first
     */
    @Throws(IOException::class)
    private fun OutputStream.writeShort(value: Int) {
        write(value and 0xff)
        write(value shr 8 and 0xff)
    }

    /**
     * Writes string to output stream
     */
    @Throws(IOException::class)
    private fun OutputStream.writeString(s: String) {
        for (element in s) {
            write(element.toInt())
        }
    }
}

/**
 * NeuQuant Neural-Net Quantization Algorithm
 * ------------------------------------------
 *
 * Copyright (c) 1994 Anthony Dekker
 *
 * NEUQUANT Neural-Net quantization algorithm by Anthony Dekker, 1994. See
 * "Kohonen neural networks for optimal colour quantization" in "Network:
 * Computation in Neural Systems" Vol. 5 (1994) pp 351-367. for a discussion of
 * the algorithm.
 *
 * Any party obtaining a copy of these files from the author, directly or
 * indirectly, is granted, free of charge, a full and unrestricted irrevocable,
 * world-wide, paid up, royalty-free, nonexclusive right and license to deal in
 * this software and documentation files (the "Software"), including without
 * limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons who
 * receive copies from any such party to do so, with the only requirement being
 * that this copyright notice remain intact.
 */

// Ported to Java 12/00 K Weiner
internal class NeuQuant(thepic: ByteArray, len: Int, sample: Int) {

    /* biased by 10 bits */
    private var alphadec: Int = 0

    /*
     * Types and Global Variables
     */

    /* the input image itself */
    private val thepicture: ByteArray = thepic
    /* lengthcount = H*W*3 */
    private val lengthcount: Int = len
    /* sampling factor 1..30 */
    private var samplefac: Int = sample
    /* the network itself - [netsize][4] */
    private val network: Array<IntArray> = Array(netsize) { IntArray(4) }
    private val netindex = IntArray(256)
    /* for network lookup - really 256 */
    private val bias = IntArray(netsize)
    /* bias and freq arrays for learning */
    private val freq = IntArray(netsize) { i -> intbias / netsize }
    private val radpower = IntArray(initrad)

    /**
     * Initialise network in range (0,0,0) to (255,255,255) and set parameters
     */
    init {
        for (i in 0 until netsize) {
            val p = network[i]
            p[2] = (i shl netbiasshift + 8) / netsize
            p[1] = p[2]
            p[0] = p[1]
        }
    }

    private fun colorMap(): ByteArray {
        val map = ByteArray(3 * netsize)
        val index = IntArray(netsize)
        for (i in 0 until netsize) {
            index[network[i][3]] = i
        }
        var k = 0
        for (i in 0 until netsize) {
            val j = index[i]
            map[k++] = network[j][0].toByte()
            map[k++] = network[j][1].toByte()
            map[k++] = network[j][2].toByte()
        }
        return map
    }

    /**
     * Insertion sort of network and building of netindex[0..255] (to do after
     * unbias)
     */
    private fun inxbuild() {
        var smallpos = 0
        var smallval = 0
        var previouscol = 0
        var startpos = 0
        for (i in 0 until netsize) {
            val p = network[i]
            smallpos = i
            smallval = p[1]
            for (j in (i + 1) until netsize) {
                val q = network[j]
                if (q[1] < smallval) {
                    smallpos = j
                    smallval = q[1]
                }
            }
            val q = network[smallpos]
            var t: Int
            if (i != smallpos) {
                t = q[0]; q[0] = p[0]; p[0] = t
                t = q[1]; q[1] = p[1]; p[1] = t
                t = q[2]; q[2] = p[2]; p[2] = t
                t = q[3]; q[3] = p[3]; p[3] = t
            }
            if (smallval != previouscol) {
                netindex[previouscol] = startpos + i shr 1
                for (k in (previouscol + 1) until smallval) {
                    netindex[k] = i
                }
                previouscol = smallval
                startpos = i
            }
        }
        netindex[previouscol] = startpos + maxnetpos shr 1
        for (j in (previouscol + 1) until 256) {
            netindex[j] = maxnetpos /* really 256 */
        }
    }

    /**
     * Main Learning Loop
     */
    private fun learn() {
        if (lengthcount < minpicturebytes) {
            samplefac = 1
        }
        alphadec = 30 + (samplefac - 1) / 3
        var alpha = initalpha
        var radius = initradius
        var rad: Int = radius shr radiusbiasshift
        val samplepixels = lengthcount / (3 * samplefac)
        var delta: Int = samplepixels / ncycles
        var pix = 0
        val lim = lengthcount
        if (rad <= 1) rad = 0
        for (i in 0 until rad) {
            radpower[i] = alpha * ((rad * rad - i * i) * radbias / (rad * rad))
        }
        // fprintf(stderr,"beginning 1D learning: initial radius=%d\n", rad);
        val step = when {
            lengthcount < minpicturebytes -> 3
            lengthcount % prime1 != 0 -> 3 * prime1
            lengthcount % prime2 != 0 -> 3 * prime2
            lengthcount % prime3 != 0 -> 3 * prime3
            else -> 3 * prime4
        }
        for (i in 0 until samplepixels) {
            val r = thepicture[pix + 0].toInt() and 0xff shl netbiasshift
            val g = thepicture[pix + 1].toInt() and 0xff shl netbiasshift
            val b = thepicture[pix + 2].toInt() and 0xff shl netbiasshift
            val j = contest(b, g, r)
            altersingle(alpha, j, b, g, r)
            if (rad != 0) {
                alterneigh(rad, j, b, g, r)
            }
            pix += step
            if (pix >= lim) {
                pix -= lengthcount
            }
            if (delta == 0) {
                delta = 1
            }
            if ((i + 1) % delta == 0) {
                alpha -= alpha / alphadec
                radius -= radius / radiusdec
                rad = radius shr radiusbiasshift
                if (rad <= 1) rad = 0
                for (k in 0 until rad) {
                    radpower[k] = alpha * ((rad * rad - k * k) * radbias / (rad * rad))
                }
            }
        }
    }

    /**
     * Search for BGR values 0..255 (after net is unbiased) and return colour
     * index
     */
    fun map(r: Int, g: Int, b: Int): Int {
        var bestd = 1000
        var best: Int = -1
        var i = netindex[g]
        var j = i - 1
        while (i < netsize || j >= 0) {
            if (i < netsize) {
                val p = network[i]
                var dist = p[1] - g
                if (dist >= bestd) {
                    i = netsize
                } else {
                    i++
                    if (dist < 0) dist = -dist
                    var a = p[0] - b
                    if (a < 0) a = -a
                    dist += a
                    if (dist < bestd) {
                        a = p[2] - r
                        if (a < 0) a = -a
                        dist += a
                        if (dist < bestd) {
                            bestd = dist
                            best = p[3]
                        }
                    }
                }
            }
            if (j >= 0) {
                val p = network[j]
                var dist = g - p[1]
                if (dist >= bestd) {
                    j = -1
                } else {
                    j--
                    if (dist < 0) dist = -dist
                    var a = p[0] - b
                    if (a < 0) a = -a
                    dist += a
                    if (dist < bestd) {
                        a = p[2] - r
                        if (a < 0) a = -a
                        dist += a
                        if (dist < bestd) {
                            bestd = dist
                            best = p[3]
                        }
                    }
                }
            }
        }
        return best
    }

    /**
     * Unbias network to give byte values 0..255 and record position i to prepare
     * for sort
     */
    private fun unbiasnet() {
        for (i in 0 until netsize) {
            network[i][0] = network[i][0] shr netbiasshift
            network[i][1] = network[i][1] shr netbiasshift
            network[i][2] = network[i][2] shr netbiasshift
            network[i][3] = i /* record colour no */
        }
    }

    fun process(): ByteArray {
        learn()
        unbiasnet()
        inxbuild()
        return colorMap()
    }

    /**
     * Move adjacent neurons by precomputed `alpha*(1-((i-j)^2/[r]^2))` in `radpower[|i-j|]`
     */
    private fun alterneigh(rad: Int, i: Int, b: Int, g: Int, r: Int) {
        val lo = (i - rad).coerceAtLeast(-1)
        val hi = (i + rad).coerceAtMost(netsize)
        var j = i + 1
        var k = i - 1
        var m = 1
        while (j < hi || k > lo) {
            val a = radpower[m++]
            if (j < hi) {
                val p = network[j++]
                try {
                    p[0] -= a * (p[0] - b) / alpharadbias
                    p[1] -= a * (p[1] - g) / alpharadbias
                    p[2] -= a * (p[2] - r) / alpharadbias
                } catch (e: Exception) {
                }
            }
            if (k > lo) {
                val p = network[k--]
                try {
                    p[0] -= a * (p[0] - b) / alpharadbias
                    p[1] -= a * (p[1] - g) / alpharadbias
                    p[2] -= a * (p[2] - r) / alpharadbias
                } catch (e: Exception) {
                }
            }
        }
    }

    /**
     * Move neuron i towards biased (b,g,r) by factor alpha
     */
    private fun altersingle(alpha: Int, i: Int, b: Int, g: Int, r: Int) {
        val n = network[i]
        n[0] -= alpha * (n[0] - b) / initalpha
        n[1] -= alpha * (n[1] - g) / initalpha
        n[2] -= alpha * (n[2] - r) / initalpha
    }

    /**
     * Search for biased BGR values
     */
    private fun contest(b: Int, g: Int, r: Int): Int {
        var bestpos: Int = -1
        var bestbiaspos: Int = bestpos
        var bestd: Int = (1 shl 31).inv()
        var bestbiasd: Int = bestd
        for (i in 0 until netsize) {
            val n = network[i]
            var dist = n[0] - b
            if (dist < 0) dist = -dist
            var a = n[1] - g
            if (a < 0) a = -a
            dist += a
            a = n[2] - r
            if (a < 0) a = -a
            dist += a
            if (dist < bestd) {
                bestd = dist
                bestpos = i
            }
            val biasdist = dist - (bias[i] shr intbiasshift - netbiasshift)
            if (biasdist < bestbiasd) {
                bestbiasd = biasdist
                bestbiaspos = i
            }
            val betafreq = freq[i] shr betashift
            freq[i] -= betafreq
            bias[i] += betafreq shl gammashift
        }
        freq[bestpos] += beta
        bias[bestpos] -= betagamma
        return bestbiaspos
    }

    companion object {

        /* number of colours used */
        private const val netsize = 256
        /* four primes near 500 - assume no image has a length so large */
        /* that it is divisible by all four primes */
        private const val prime1 = 499
        private const val prime2 = 491
        private const val prime3 = 487
        private const val prime4 = 503
        /* minimum size for input image */
        private const val minpicturebytes = 3 * prime4

        /*
         * Network Definitions
         */

        private const val maxnetpos = netsize - 1
        /* bias for colour values */
        private const val netbiasshift = 4
        /* no. of learning cycles */
        private const val ncycles = 100
        /* defs for freq and bias */
        private const val intbiasshift = 16
        /* bias for fractions */
        private const val intbias = 1 shl intbiasshift
        private const val gammashift = 10
        /* gamma = 1024 */
        private const val gamma = 1 shl gammashift
        private const val betashift = 10
        /* beta = 1/1024 */
        private const val beta = intbias shr betashift
        private const val betagamma = intbias shl gammashift - betashift
        /* defs for decreasing radius factor */
        private const val initrad = netsize shr 3
        /* for 256 cols, radius starts */
        private const val radiusbiasshift = 6
        /* at 32.0 biased by 6 bits */
        private const val radiusbias = 1 shl radiusbiasshift
        private const val initradius = initrad * radiusbias
        /* and decreases by a */
        /* factor of 1/30 each cycle */
        private const val radiusdec = 30
        /* defs for decreasing alpha factor */
        /* alpha starts at 1.0 */
        private const val alphabiasshift = 10
        private const val initalpha = 1 shl alphabiasshift
        /* radbias and alpharadbias used for radpower calculation */
        private const val radbiasshift = 8
        private const val radbias = 1 shl radbiasshift
        private const val alpharadbshift = alphabiasshift + radbiasshift
        private const val alpharadbias = 1 shl alpharadbshift
    }
}

// Adapted from Jef Poskanzer's Java port by way of J. M. G. Elliott.
// K Weiner 12/00
internal class LZWEncoder(
    private val imgW: Int,
    private val imgH: Int,
    private val pixAry: ByteArray?,
    color_depth: Int
) {

    private val initCodeSize: Int = 2.coerceAtLeast(color_depth)
    private var remaining = 0
    private var curPixel = 0

    // GIF Image compression - modified 'compress'
    //
    // Based on: compress.c - File compression ala IEEE Computer, June 1984.
    //
    // By Authors: Spencer W. Thomas (decvax!harpo!utah-cs!utah-gr!thomas)
    // Jim McKie (decvax!mcvax!jim)
    // Steve Davies (decvax!vax135!petsd!peora!srd)
    // Ken Turkowski (decvax!decwrl!turtlevax!ken)
    // James A. Woods (decvax!ihnp4!ames!jaw)
    // Joe Orost (decvax!vax135!petsd!joe)

    // number of bits/code
    private var n_bits = 0
    // user settable max # bits/code
    private var maxbits = BITS
    // maximum code, given n_bits
    private var maxcode = 0
    // should NEVER generate this code
    private var maxmaxcode = 1 shl BITS
    private var htab = IntArray(HSIZE)
    private var codetab = IntArray(HSIZE)
    // for dynamic table sizing
    private var hsize = HSIZE
    // first unused entry
    private var free_ent = 0
    // block compression parameters -- after all codes are used up,
    // and compression rate changes, start over.
    private var clear_flg = false
    // Algorithm: use open addressing double hashing (no chaining) on the
    // prefix code / next character combination. We do a variant of Knuth's
    // algorithm D (vol. 3, sec. 6.4) along with G. Knott's relatively-prime
    // secondary probe. Here, the modular division first probe is gives way
    // to a faster exclusive-or manipulation. Also do block compression with
    // an adaptive reset, whereby the code table is cleared when the compression
    // ratio decreases, but after the table fills. The variable-length output
    // codes are re-sized at this point, and a special CLEAR code is generated
    // for the decompressor. Late addition: construct the table according to
    // file size for noticeable speed improvement on small files. Please direct
    // questions about this implementation to ames!jaw.
    private var g_init_bits = 0
    private var ClearCode = 0
    private var EOFCode = 0
    // output
    //
    // Output the given code.
    // Inputs:
    // code: A n_bits-bit integer. If == -1, then EOF. This assumes
    // that n_bits =< wordsize - 1.
    // Outputs:
    // Outputs code to the file.
    // Assumptions:
    // Chars are 8 bits long.
    // Algorithm:
    // Maintain a BITS character long buffer (so that 8 codes will
    // fit in it exactly). Use the VAX insv instruction to insert each
    // code in turn. When the buffer fills up empty it and start over.
    private var cur_accum = 0
    private var cur_bits = 0
    private var masks = intArrayOf(
        0x0000, 0x0001, 0x0003, 0x0007, 0x000F, 0x001F, 0x003F, 0x007F, 0x00FF, 0x01FF,
        0x03FF, 0x07FF, 0x0FFF, 0x1FFF, 0x3FFF, 0x7FFF, 0xFFFF
    )
    // Number of characters so far in this 'packet'
    private var a_count = 0
    // Define the storage for the packet accumulator
    private var accum = ByteArray(256)

    // Add a character to the end of the current packet, and if it is 254
    // characters, flush the packet to disk.
    @Throws(IOException::class)
    fun char_out(c: Byte, outs: OutputStream) {
        accum[a_count++] = c
        if (a_count >= 254) flush_char(outs)
    }

    // Clear out the hash table
    // table clear for block compress
    @Throws(IOException::class)
    fun cl_block(outs: OutputStream) {
        cl_hash(hsize)
        free_ent = ClearCode + 2
        clear_flg = true
        output(ClearCode, outs)
    }

    // reset code table
    private fun cl_hash(hsize: Int) {
        for (i in 0 until hsize) htab[i] = -1
    }

    @Throws(IOException::class)
    fun compress(init_bits: Int, outs: OutputStream) {
        var fcode: Int
        var i /* = 0 */: Int
        var c: Int
        var ent: Int
        var disp: Int
        // Set up the globals: g_init_bits - initial number of bits
        g_init_bits = init_bits
        // Set up the necessary values
        clear_flg = false
        n_bits = g_init_bits
        maxcode = MAXCODE(n_bits)
        ClearCode = 1 shl init_bits - 1
        EOFCode = ClearCode + 1
        free_ent = ClearCode + 2
        a_count = 0 // clear packet
        ent = nextPixel()
        var hshift = 0
        fcode = hsize
        while (fcode < 65536) {
            ++hshift
            fcode *= 2
        }
        hshift = 8 - hshift // set hash code range bound
        val hsize_reg = hsize
        cl_hash(hsize_reg) // clear hash table
        output(ClearCode, outs)
        outer_loop@ while (nextPixel().also { c = it } != EOF) {
            fcode = (c shl maxbits) + ent
            i = c shl hshift xor ent // xor hashing
            if (htab[i] == fcode) {
                ent = codetab[i]
                continue
            } else if (htab[i] >= 0) // non-empty slot
            {
                disp = hsize_reg - i // secondary hash (after G. Knott)
                if (i == 0) disp = 1
                do {
                    if (disp.let { i -= it; i } < 0) i += hsize_reg
                    if (htab[i] == fcode) {
                        ent = codetab[i]
                        continue@outer_loop
                    }
                } while (htab[i] >= 0)
            }
            output(ent, outs)
            ent = c
            if (free_ent < maxmaxcode) {
                codetab[i] = free_ent++ // code -> hashtable
                htab[i] = fcode
            } else cl_block(outs)
        }
        // Put out the final code.
        output(ent, outs)
        output(EOFCode, outs)
    }

    // ----------------------------------------------------------------------------
    @Throws(IOException::class)
    fun encode(os: OutputStream) {
        os.write(initCodeSize) // write "initial code size" byte
        remaining = imgW * imgH // reset navigation variables
        curPixel = 0
        compress(initCodeSize + 1, os) // compress and write the pixel data
        os.write(0) // write block terminator
    }

    // Flush the packet to disk, and reset the accumulator
    @Throws(IOException::class)
    fun flush_char(outs: OutputStream) {
        if (a_count > 0) {
            outs.write(a_count)
            outs.write(accum, 0, a_count)
            a_count = 0
        }
    }

    fun MAXCODE(n_bits: Int): Int {
        return (1 shl n_bits) - 1
    }

    /**
     * Return the next pixel from the image
     */
    private fun nextPixel(): Int {
        if (remaining == 0) return EOF
        --remaining
        val pix = pixAry!![curPixel++].toInt()
        return pix and 0xff
    }

    @Throws(IOException::class)
    fun output(code: Int, outs: OutputStream) {
        cur_accum = cur_accum and masks[cur_bits]
        cur_accum = if (cur_bits > 0) cur_accum or (code shl cur_bits) else code
        cur_bits += n_bits
        while (cur_bits >= 8) {
            char_out((cur_accum and 0xff).toByte(), outs)
            cur_accum = cur_accum shr 8
            cur_bits -= 8
        }
        // If the next entry is going to be too big for the code size,
        // then increase it, if possible.
        if (free_ent > maxcode || clear_flg) {
            if (clear_flg) {
                maxcode = MAXCODE(g_init_bits.also { n_bits = it })
                clear_flg = false
            } else {
                ++n_bits
                maxcode = if (n_bits == maxbits) maxmaxcode else MAXCODE(n_bits)
            }
        }
        if (code == EOFCode) { // At EOF, write the rest of the buffer.
            while (cur_bits > 0) {
                char_out((cur_accum and 0xff).toByte(), outs)
                cur_accum = cur_accum shr 8
                cur_bits -= 8
            }
            flush_char(outs)
        }
    }

    companion object {
        private const val EOF = -1
        const val BITS = 12
        const val HSIZE = 5003 // 80% occupancy
    }
}
