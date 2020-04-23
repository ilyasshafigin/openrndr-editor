package ru.ilyasshafigin.openrndr.editor.shape.mesh

import org.openrndr.math.Vector2
import quickhull3d.QuickHull3D

object Hull {

    fun from(points: List<Vector2>): Polygon {
        if (points.size < 3) {
            return Polygon(points)
        }

        // build points array for qhull
        val qPoints = DoubleArray(points.size * 3 + 3)
        var avgX = 0.0
        var avgY = 0.0
        for (i in points.indices) {
            qPoints[i * 3 + 0] = points[i][0]
            qPoints[i * 3 + 1] = points[i][1]
            qPoints[i * 3 + 2] = 0.0
            avgX += points[i][0]
            avgY += points[i][1]
        }

        qPoints[qPoints.size - 3] = avgX / (qPoints.size - 1)
        qPoints[qPoints.size - 2] = avgY / (qPoints.size - 1)
        qPoints[qPoints.size - 1] = 10000.0

        // prepare quickhull
        val quickHull = QuickHull3D(qPoints)
        val faces = quickHull.getFaces(QuickHull3D.POINT_RELATIVE + QuickHull3D.CLOCKWISE)
        lateinit var extrema: IntArray

        // find extrema
        for (i in faces.indices) {
            var isFace = true
            for (j in faces[i].indices) {
                if (faces[i][j] == points.size) {
                    isFace = false
                    break
                }
            }
            if (isFace) {
                extrema = faces[i]
                break
            }
        }

        // make polygon
        return Polygon(extrema.map { e -> points[e] })
    }
}
