package ru.ilyasshafigin.openrndr.editor.shape.mesh

import org.openrndr.math.Vector2
import quickhull3d.QuickHull3D

object Voronoi {

    fun from(points: List<Vector2>): List<Polygon> {
        if (points.isEmpty()) {
            return emptyList()
        }

        // build points array for qhull
        val qPoints = DoubleArray(points.size * 3 + 9)
        for (i in points.indices) {
            qPoints[i * 3 + 0] = points[i][0]
            qPoints[i * 3 + 1] = points[i][1]
            qPoints[i * 3 + 2] = -(points[i][0] * points[i][0] + points[i][1] * points[i][1])
        }
        // 1
        qPoints[qPoints.size - 9] = -8000.0
        qPoints[qPoints.size - 8] = 0.0
        qPoints[qPoints.size - 7] = -64000000.0
        // 2
        qPoints[qPoints.size - 6] = 8000.0
        qPoints[qPoints.size - 5] = 8000.0
        qPoints[qPoints.size - 4] = -128000000.0
        // 3
        qPoints[qPoints.size - 3] = 8000.0
        qPoints[qPoints.size - 2] = -8000.0
        qPoints[qPoints.size - 1] = -128000000.0

        // prepare quickhull
        val quickHull = QuickHull3D(qPoints)
        val faces = quickHull.getFaces(QuickHull3D.POINT_RELATIVE + QuickHull3D.CLOCKWISE)
        var artifact = 0

        // compute dual points
        val dualPoints = Array(faces.size) { Vector2.ZERO }
        for (i in faces.indices) {
            // test if it's the artifact
            if (faces[i][0] >= points.size && faces[i][1] >= points.size && faces[i][2] >= points.size) {
                artifact = i
            }

            val x0 = qPoints[faces[i][0] * 3 + 0]
            val y0 = qPoints[faces[i][0] * 3 + 1]
            val x1 = qPoints[faces[i][1] * 3 + 0]
            val y1 = qPoints[faces[i][1] * 3 + 1]
            val x2 = qPoints[faces[i][2] * 3 + 0]
            val y2 = qPoints[faces[i][2] * 3 + 1]

            val v1x = 2 * (x1 - x0)
            val v1y = 2 * (y1 - y0)
            val v1z = x0 * x0 - x1 * x1 + y0 * y0 - y1 * y1

            val v2x = 2 * (x2 - x0)
            val v2y = 2 * (y2 - y0)
            val v2z = x0 * x0 - x2 * x2 + y0 * y0 - y2 * y2

            val tmpx = v1y * v2z - v1z * v2y
            val tmpy = v1z * v2x - v1x * v2z
            val tmpz = v1x * v2y - v1y * v2x

            dualPoints[i] = Vector2(tmpx / tmpz, tmpy / tmpz)
        }

        // create edge/point/face network
        val edges = mutableListOf<Edge>()
        val faceNet = LinkedArray(faces.size)
        val pointBuckets = Array<MutableList<Int>>(points.size) { mutableListOf() }
        for (i in faces.indices) {
            // bin faces to the points they belong with
            for (f in faces[i].indices) {
                if (faces[i][f] < points.size) {
                    pointBuckets[faces[i][f]].add(i)
                }
            }

            for (j in 0 until i) {
                if (i != artifact && j != artifact && isEdgeShared(faces[i], faces[j])) {
                    faceNet.link(i, j)
                    edges += Edge(dualPoints[i], dualPoints[j])
                }
            }
        }

        // calculate the region for each point
        val regions = mutableListOf<Polygon>()
        val faceOrder = mutableListOf<Int>()
        for (i in points.indices) {
            faceOrder.clear()

            // add coords of the region in the order they touch, starting with the convenient first
            var p = pointBuckets[i][0]
            while (p >= 0) {
                faceOrder.add(p)

                // find the next coordinate that is in this set that we haven't used yet
                var newP = -1
                for (k in 0 until faceNet[p].linkCount()) {
                    val neighbor = faceNet[p].link(k)
                    if (neighbor !in faceOrder && neighbor in pointBuckets[i]) {
                        newP = neighbor
                        break
                    }
                }
                p = newP
            }

            // turn the coordinates into a polygon
            regions += Polygon(faceOrder.map { face -> dualPoints[face] })
        }

        return regions
    }

    private fun isEdgeShared(face1: IntArray, face2: IntArray): Boolean {
        for (i in face1.indices) {
            val cur = face1[i]
            val next = face1[(i + 1) % face1.size]
            for (j in face2.indices) {
                val from = face2[j]
                val to = face2[(j + 1) % face2.size]
                if (cur == from && next == to || cur == to && next == from) return true
            }
        }
        return false
    }
}
