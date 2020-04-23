package ru.ilyasshafigin.openrndr.editor.shape.mesh

import ru.ilyasshafigin.openrndr.editor.math.EPSILON
import org.openrndr.math.Vector2
import org.openrndr.math.Vector3
import java.util.*
import kotlin.math.abs
import kotlin.math.sqrt

object Triangulate {

    fun from(points: List<Vector2>): List<Triangle> {
        if (points.isEmpty()) return emptyList()
        val pxyz = points.sortedWith(xComparator)
        var xmin: Double = pxyz[0].x
        var ymin: Double = pxyz[0].y
        var xmax = xmin
        var ymax = ymin
        var pIter: Iterator<Vector2> = pxyz.iterator()
        while (pIter.hasNext()) {
            val p = pIter.next()
            if (p.x < xmin) xmin = p.x
            if (p.x > xmax) xmax = p.x
            if (p.y < ymin) ymin = p.y
            if (p.y > ymax) ymax = p.y
        }
        val dx = xmax - xmin
        val dy = ymax - ymin
        val dmax = if (dx > dy) dx else dy
        val xmid = (xmax + xmin) / 2.0
        val ymid = (ymax + ymin) / 2.0
        val triangles = ArrayList<Triangle>()
        val complete = HashSet<Triangle>()

        val superTriangle = Triangle(
            p1 = Vector2(xmid - 2.0 * dmax, ymid - dmax),
            p2 = Vector2(xmid, ymid + 2.0 * dmax),
            p3 = Vector2(xmid + 2.0 * dmax, ymid - dmax)
        )
        triangles.add(superTriangle)

        val edges = ArrayList<Edge>()
        pIter = pxyz.iterator()
        while (pIter.hasNext()) {
            val p = pIter.next()
            edges.clear()

            for (j in triangles.indices.reversed()) {
                val t = triangles[j]
                if (complete.contains(t)) {
                    continue
                }
                val (circle, inside) = circumCircle(p, t)
                if (circle.x + circle.z < p.x) {
                    complete.add(t)
                }
                if (inside) {
                    edges.add(Edge(t.p1, t.p2))
                    edges.add(Edge(t.p2, t.p3))
                    edges.add(Edge(t.p3, t.p1))
                    triangles.removeAt(j)
                }
            }

            for (j in 0 until edges.size - 1) {
                val e1 = edges[j]
                for (k in (j + 1) until edges.size) {
                    val e2 = edges[k]
                    if (e1.p1 === e2.p2 && e1.p2 === e2.p1 || e1.p1 === e2.p1 && e1.p2 === e2.p2) {
                        edges[j] = Edge.ZERO
                        edges[k] = Edge.ZERO
                    }
                }
            }

            for (j in edges.indices) {
                val e = edges[j]
                if (e == Edge.ZERO) {
                    continue
                }
                triangles.add(Triangle(e.p1, e.p2, p))
            }
        }

        for (i in triangles.indices.reversed()) {
            if (triangles[i].sharesVertex(superTriangle)) {
                triangles.removeAt(i)
            }
        }
        return triangles
    }

    private fun circumCircle(p: Vector2, t: Triangle): Pair<Vector3, Boolean> {
        val m1: Double
        val m2: Double
        val mx1: Double
        val mx2: Double
        val my1: Double
        val my2: Double
        val cx: Double
        val cy: Double
        if (abs(t.p1.y - t.p2.y) < EPSILON && abs(t.p2.y - t.p3.y) < EPSILON) {
            return Vector3(0.0, 0.0, 0.0) to false
        }
        when {
            abs(t.p2.y - t.p1.y) < EPSILON -> {
                m2 = -(t.p3.x - t.p2.x) / (t.p3.y - t.p2.y)
                mx2 = (t.p2.x + t.p3.x) / 2.0
                my2 = (t.p2.y + t.p3.y) / 2.0
                cx = (t.p2.x + t.p1.x) / 2.0
                cy = m2 * (cx - mx2) + my2
            }
            abs(t.p3.y - t.p2.y) < EPSILON -> {
                m1 = -(t.p2.x - t.p1.x) / (t.p2.y - t.p1.y)
                mx1 = (t.p1.x + t.p2.x) / 2.0
                my1 = (t.p1.y + t.p2.y) / 2.0
                cx = (t.p3.x + t.p2.x) / 2.0
                cy = m1 * (cx - mx1) + my1
            }
            else -> {
                m1 = -(t.p2.x - t.p1.x) / (t.p2.y - t.p1.y)
                m2 = -(t.p3.x - t.p2.x) / (t.p3.y - t.p2.y)
                mx1 = (t.p1.x + t.p2.x) / 2.0
                mx2 = (t.p2.x + t.p3.x) / 2.0
                my1 = (t.p1.y + t.p2.y) / 2.0
                my2 = (t.p2.y + t.p3.y) / 2.0
                cx = (m1 * mx1 - m2 * mx2 + my2 - my1) / (m1 - m2)
                cy = m1 * (cx - mx1) + my1
            }
        }
        var dx = t.p2.x - cx
        var dy = t.p2.y - cy
        val rsqr = dx * dx + dy * dy
        val z = sqrt(rsqr)
        dx = p.x - cx
        dy = p.y - cy
        val drsqr = dx * dx + dy * dy
        return Vector3(cx, cy, z) to (drsqr <= rsqr)
    }

    private val xComparator = Comparator<Vector2> { p1, p2 ->
        when {
            p1.x < p2.x -> -1
            p1.x > p2.x -> 1
            else -> 0
        }
    }
}
