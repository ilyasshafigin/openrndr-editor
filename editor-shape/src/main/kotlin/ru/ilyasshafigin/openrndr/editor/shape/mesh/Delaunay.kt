package ru.ilyasshafigin.openrndr.editor.shape.mesh

import org.openrndr.math.Vector2
import quickhull3d.QuickHull3D

object Delaunay {

    fun from(points: List<Vector2>): List<Edge> {
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
        qPoints[qPoints.size - 9] = -2000.0
        qPoints[qPoints.size - 8] = 0.0
        qPoints[qPoints.size - 7] = -4000000.0
        // 2
        qPoints[qPoints.size - 6] = 2000.0
        qPoints[qPoints.size - 5] = 2000.0
        qPoints[qPoints.size - 4] = -8000000.0
        // 3
        qPoints[qPoints.size - 3] = 2000.0
        qPoints[qPoints.size - 2] = -2000.0
        qPoints[qPoints.size - 1] = -8000000.0

        // prepare quickhull
        val quickHull = QuickHull3D(qPoints)
        val faces = quickHull.getFaces(QuickHull3D.POINT_RELATIVE + QuickHull3D.CLOCKWISE)

        // turn faces into links
        val mesh = LinkedArray(points.size + 3)
        val links = mutableListOf<Link>()
        for (i in faces.indices) {
            for (j in faces[i].indices) {
                val p = faces[i][j]
                val q = faces[i][(j + 1) % faces[i].size]
                if (p < points.size && q < points.size && !mesh.linked(p, q)) {
                    mesh.link(p, q)
                    links += Link(p, q)
                }
            }
        }

        // turn links into edges
        return links.map { link -> Edge(points[link.p], points[link.q]) }
    }
}
