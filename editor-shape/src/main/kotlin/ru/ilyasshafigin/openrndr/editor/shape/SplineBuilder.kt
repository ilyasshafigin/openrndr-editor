package ru.ilyasshafigin.openrndr.editor.shape

import org.openrndr.math.Matrix44
import org.openrndr.math.Vector2
import org.openrndr.shape.Segment
import org.openrndr.shape.ShapeContour

class SplineBuilder(
    private val multipleContours: Boolean,
    private var curveDetail: Int = DEFAULT_DETAIL,
    private var curveTightness: Double = DEFAULT_THICKNESS
) {

    private var cursor = Vector2.INFINITY
    private var anchor = Vector2.INFINITY

    private val segments = mutableListOf<Segment>()
    private val contours = mutableListOf<ShapeContour>()

    val result: List<ShapeContour>
        get() {
            return contours + if (segments.isNotEmpty()) {
                listOf(ShapeContour(segments.map { it }, false))
            } else {
                emptyList()
            }
        }

    private var curveBasisMatrix: Matrix44 = Matrix44.IDENTITY
    private var curveDrawMatrix: Matrix44 = Matrix44.IDENTITY
    private val curveVertices: MutableList<Vector2> = mutableListOf()

    init {
        init()
    }

    private fun init() {
        val s = curveTightness
        val d = curveDetail

        val f = 1.0 / d
        val ff = f * f
        val fff = ff * f

        curveBasisMatrix = Matrix44(
            c0r0 = (s - 1) / 2, c1r0 = (s + 3) / 2, c2r0 = (-3 - s) / 2.0, c3r0 = (1 - s) / 2,
            c0r1 = (1 - s), c1r1 = (-5 - s) / 2, c2r1 = (s + 2), c3r1 = (s - 1) / 2,
            c0r2 = (s - 1) / 2, c1r2 = 0.0, c2r2 = (1 - s) / 2, c3r2 = 0.0,
            c0r3 = 0.0, c1r3 = 1.0, c2r3 = 0.0, c3r3 = 0.0
        )

        curveDrawMatrix = Matrix44(
            c0r0 = 0.0, c1r0 = 0.0, c2r0 = 0.0, c3r0 = 1.0,
            c0r1 = fff, c1r1 = ff, c2r1 = f, c3r1 = 0.0,
            c0r2 = 6 * fff, c1r2 = 2 * ff, c2r2 = 0.0, c3r2 = 0.0,
            c0r3 = 6 * fff, c1r3 = 0.0, c2r3 = 0.0, c3r3 = 0.0
        )

        curveDrawMatrix *= curveBasisMatrix
    }

    fun isCanClose() = segments.isNotEmpty()

    fun close() {
        require(segments.isNotEmpty()) { "cannot close contour with 0 segments" }

        if ((anchor - cursor).length > 0.001) {
            segments.add(Segment(cursor, anchor))
        }
        contours.add(ShapeContour(segments.map { it }, true))
        segments.clear()

        anchor = Vector2.INFINITY
        curveVertices.clear()
    }

    fun reverse() {
        segments.forEachIndexed { index, segment ->
            segments[index] = segment.reverse
        }
        segments.reverse()
    }

    fun next() {
        if (multipleContours && segments.isNotEmpty()) {
            contours.add(ShapeContour(segments.map { it }, false))
            segments.clear()
        }
        anchor = Vector2.INFINITY
        curveVertices.clear()
    }

    /**
     * Sets the resolution at which curves display. The default value is 20.
     */
    fun detail(detail: Int) {
        curveDetail = detail
        init()
    }

    /**
     * Modifies the quality of forms created with [vertex]& The parameter [tightness] determines how
     * the curve fits to the vertex points. The value 0.0 is the default value for [tightness]
     * (this value defines the curves to be Catmull-Rom splines) and the value 1.0 connects all
     * the points with straight lines. Values within the range -5.0 and 5.0 will deform the curves
     * but will leave them recognizable and as values increase in magnitude, they will continue to deform.
     */
    fun tightness(tightness: Double) {
        curveTightness = tightness
        init()
    }

    fun vertex(x: Double, y: Double) {
        vertex(Vector2(x, y))
    }

    fun vertex(vertex: Vector2) {
        curveVertices += vertex

        if (curveVertices.size > 3) {
            val lastIndex = curveVertices.lastIndex
            curveVertexSegment(
                curveVertices[lastIndex - 3],
                curveVertices[lastIndex - 2],
                curveVertices[lastIndex - 1],
                curveVertices[lastIndex]
            )
        }
    }

    private fun curveVertexSegment(
        v1: Vector2,
        v2: Vector2,
        v3: Vector2,
        v4: Vector2
    ) {
        var x0 = v2.x
        var y0 = v2.y
        val draw = curveDrawMatrix
        var xplot1 = draw.c0r1 * v1.x + draw.c1r1 * v2.x + draw.c2r1 * v3.x + draw.c3r1 * v4.x
        var xplot2 = draw.c0r2 * v1.x + draw.c1r2 * v2.x + draw.c2r2 * v3.x + draw.c3r2 * v4.x
        val xplot3 = draw.c0r3 * v1.x + draw.c1r3 * v2.x + draw.c2r3 * v3.x + draw.c3r3 * v4.x
        var yplot1 = draw.c0r1 * v1.y + draw.c1r1 * v2.y + draw.c2r1 * v3.y + draw.c3r1 * v4.y
        var yplot2 = draw.c0r2 * v1.y + draw.c1r2 * v2.y + draw.c2r2 * v3.y + draw.c3r2 * v4.y
        val yplot3 = draw.c0r3 * v1.y + draw.c1r3 * v2.y + draw.c2r3 * v3.y + draw.c3r3 * v4.y
        moveOrLineTo(x0, y0)
        repeat(curveDetail) {
            x0 += xplot1; xplot1 += xplot2; xplot2 += xplot3
            y0 += yplot1; yplot1 += yplot2; yplot2 += yplot3
            moveOrLineTo(x0, y0)
        }
    }

    private fun moveTo(position: Vector2) {
        require(multipleContours || anchor === Vector2.INFINITY) { "pen only can only be moved once per contour" }
        if (multipleContours && segments.isNotEmpty()) {
            contours.add(ShapeContour(segments.map { it }, false))
            segments.clear()
        }
        cursor = position
        anchor = position
    }

    private fun moveOrLineTo(position: Vector2) {
        if (anchor === Vector2.INFINITY) {
            moveTo(position)
        } else {
            lineTo(position)
        }
    }

    private fun moveOrLineTo(x: Double, y: Double) = moveOrLineTo(Vector2(x, y))

    private fun lineTo(position: Vector2) {
        require(cursor !== Vector2.INFINITY) { "use moveTo first" }
        if ((position - cursor).length > 0.0) {
            val segment = Segment(cursor, position)
            segments.add(segment)
            cursor = position
        }
    }

    companion object {

        const val DEFAULT_THICKNESS = 0.0
        const val DEFAULT_DETAIL = 5
    }
}

fun spline(
    detail: Int = SplineBuilder.DEFAULT_DETAIL,
    tightness: Double = SplineBuilder.DEFAULT_THICKNESS,
    f: SplineBuilder.() -> Unit
): ShapeContour {
    val sb = SplineBuilder(false, detail, tightness)
    sb.f()
    return sb.result.first()
}

fun splines(
    detail: Int = SplineBuilder.DEFAULT_DETAIL,
    tightness: Double = SplineBuilder.DEFAULT_THICKNESS,
    f: SplineBuilder.() -> Unit
): List<ShapeContour> {
    val sb = SplineBuilder(true, detail, tightness)
    sb.f()
    return sb.result
}
