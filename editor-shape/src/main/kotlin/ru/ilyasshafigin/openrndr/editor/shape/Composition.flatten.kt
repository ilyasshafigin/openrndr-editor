package ru.ilyasshafigin.openrndr.editor.shape

import org.openrndr.shape.Composition
import org.openrndr.shape.ShapeNode
import org.openrndr.shape.map

fun Composition.flatten(): Composition = Composition(root.map { node ->
    if (node is ShapeNode) node.flatten() else node
})
