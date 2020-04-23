package ru.ilyasshafigin.openrndr.editor.math.grid

import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Hex grid
 * Based on https://www.redblobgames.com/grids/hexagons/
 */
class HexGrid {

    data class Point(val x: Double, val y: Double)

    data class Hex(val q: Int, val r: Int, val s: Int) {

        init {
            require(q + r + s == 0) { "q + r + s must be 0" }
        }

        operator fun plus(b: Hex): Hex = Hex(q + b.q, r + b.r, s + b.s)
        operator fun minus(b: Hex): Hex = Hex(q - b.q, r - b.r, s - b.s)
        operator fun times(k: Int): Hex = Hex(q * k, r * k, s * k)

        fun rotateLeft(): Hex = Hex(-s, -q, -r)
        fun rotateRight(): Hex = Hex(-r, -s, -q)

        fun neighbor(direction: Int): Hex = this + direction(direction)
        fun diagonalNeighbor(direction: Int): Hex = this + DIAGONALS[direction]

        val length: Int
            get() = (abs(q) + abs(r) + abs(
                s
            )) / 2

        fun distance(b: Hex): Int = (this - b).length

        companion object {

            val DIRECTIONS = listOf(
                Hex(1, 0, -1),
                Hex(1, -1, 0),
                Hex(0, -1, 1),
                Hex(-1, 0, 1),
                Hex(-1, 1, 0),
                Hex(0, 1, -1)
            )

            val DIAGONALS = listOf(
                Hex(2, -1, -1),
                Hex(1, -2, 1),
                Hex(-1, -1, 2),
                Hex(-2, 1, 1),
                Hex(-1, 2, -1),
                Hex(1, 1, -2)
            )

            fun direction(direction: Int): Hex {
                return DIRECTIONS[direction]
            }
        }
    }

    data class FractionalHex(val q: Double, val r: Double, val s: Double) {

        init {
            require((q + r + s).roundToInt() == 0) { "q + r + s must be 0" }
        }

        fun hexRound(): Hex {
            var qi = q.roundToInt()
            var ri = r.roundToInt()
            var si = s.roundToInt()
            val dq = abs(qi - q)
            val dr = abs(ri - r)
            val ds = abs(si - s)
            if (dq > dr && dq > ds) {
                qi = -ri - si
            } else if (dr > ds) {
                ri = -qi - si
            } else {
                si = -qi - ri
            }
            return Hex(qi, ri, si)
        }

        fun hexLerp(b: FractionalHex, t: Double): FractionalHex {
            return FractionalHex(q * (1.0 - t) + b.q * t, r * (1.0 - t) + b.r * t, s * (1.0 - t) + b.s * t)
        }

        companion object {

            fun hexLinedraw(a: Hex, b: Hex): List<Hex> {
                val N = a.distance(b)
                val a_nudge = FractionalHex(a.q + 1e-06, a.r + 1e-06, a.s - 2e-06)
                val b_nudge = FractionalHex(b.q + 1e-06, b.r + 1e-06, b.s - 2e-06)
                val results = mutableListOf<Hex>()
                val step = 1.0 / N.coerceAtLeast(1)
                for (i in 0..N) {
                    results.add(a_nudge.hexLerp(b_nudge, step * i).hexRound())
                }
                return results
            }
        }
    }

    data class OffsetCoord(val col: Int, val row: Int) {

        companion object {

            const val EVEN = 1
            const val ODD = -1

            fun qoffsetFromCube(offset: Int, h: Hex): OffsetCoord {
                val col = h.q
                val row = h.r + ((h.q + offset * (h.q and 1)) / 2)
                require(!(offset != EVEN && offset != ODD)) { "offset must be EVEN (+1) or ODD (-1)" }
                return OffsetCoord(col, row)
            }

            fun qoffsetToCube(offset: Int, h: OffsetCoord): Hex {
                val q = h.col
                val r = h.row - ((h.col + offset * (h.col and 1)) / 2)
                val s = -q - r
                require(!(offset != EVEN && offset != ODD)) { "offset must be EVEN (+1) or ODD (-1)" }
                return Hex(q, r, s)
            }

            fun roffsetFromCube(offset: Int, h: Hex): OffsetCoord {
                val col = h.q + ((h.r + offset * (h.r and 1)) / 2)
                val row = h.r
                require(!(offset != EVEN && offset != ODD)) { "offset must be EVEN (+1) or ODD (-1)" }
                return OffsetCoord(col, row)
            }

            fun roffsetToCube(offset: Int, h: OffsetCoord): Hex {
                val q = h.col - ((h.row + offset * (h.row and 1)) / 2)
                val r = h.row
                val s = -q - r
                require(!(offset != EVEN && offset != ODD)) { "offset must be EVEN (+1) or ODD (-1)" }
                return Hex(q, r, s)
            }
        }
    }

    data class DoubledCoord(val col: Int, val row: Int) {

        fun qdoubledToCube(): Hex {
            val q = col
            val r = ((row - col) / 2)
            val s = -q - r
            return Hex(q, r, s)
        }

        fun rdoubledToCube(): Hex {
            val q = ((col - row) / 2)
            val r = row
            val s = -q - r
            return Hex(q, r, s)
        }

        companion object {

            fun qdoubledFromCube(h: Hex): DoubledCoord {
                val col = h.q
                val row = 2 * h.r + h.q
                return DoubledCoord(col, row)
            }

            fun rdoubledFromCube(h: Hex): DoubledCoord {
                val col = 2 * h.q + h.r
                val row = h.r
                return DoubledCoord(col, row)
            }
        }
    }

    data class Orientation(
        val f0: Double,
        val f1: Double,
        val f2: Double,
        val f3: Double,
        val b0: Double,
        val b1: Double,
        val b2: Double,
        val b3: Double,
        val startAngle: Double
    ) {

        companion object {

            val POINTY = Orientation(
                sqrt(3.0),
                sqrt(3.0) / 2.0,
                0.0,
                3.0 / 2.0,
                sqrt(3.0) / 3.0,
                -1.0 / 3.0,
                0.0,
                2.0 / 3.0,
                0.5
            )

            val FLAT = Orientation(
                3.0 / 2.0,
                0.0,
                sqrt(3.0) / 2.0,
                sqrt(3.0),
                2.0 / 3.0,
                0.0,
                -1.0 / 3.0,
                sqrt(3.0) / 3.0,
                0.0
            )
        }
    }

    data class Layout(val orientation: Orientation, val size: Point, val origin: Point) {

        fun hexToPixel(h: Hex): Point {
            val (f0, f1, f2, f3) = orientation
            val x = (f0 * h.q + f1 * h.r) * size.x
            val y = (f2 * h.q + f3 * h.r) * size.y
            return Point(x + origin.x, y + origin.y)
        }

        fun pixelToHex(p: Point): FractionalHex {
            val (_, _, _, _, b0, b1, b2, b3) = orientation
            val (x, y) = Point((p.x - origin.x) / size.x, (p.y - origin.y) / size.y)
            val q = b0 * x + b1 * y
            val r = b2 * x + b3 * y
            return FractionalHex(q, r, -q - r)
        }

        fun hexCornerOffset(corner: Int): Point {
            val angle: Double = 2.0 * Math.PI * (orientation.startAngle - corner) / 6.0
            return Point(size.x * cos(angle), size.y * sin(
                angle
            )
            )
        }

        fun polygonCorners(h: Hex): List<Point> {
            val corners = mutableListOf<Point>()
            val (x, y) = hexToPixel(h)
            for (i in 0..5) {
                val (x1, y1) = hexCornerOffset(i)
                corners.add(Point(x + x1, y + y1))
            }
            return corners
        }
    }
}
