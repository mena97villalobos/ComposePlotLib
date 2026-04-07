package com.mena97villalobos.composeplotlib.core.layout

/**
 * Depth-first traversal of a command tree, visiting each node including [DrawCommand.Group]
 * and [DrawCommand.ClipRect] containers before their children.
 */
fun DrawCommand.visitDepthFirst(onEach: (DrawCommand) -> Unit) {
    onEach(this)
    when (this) {
        is DrawCommand.ClipRect -> children.forEach { it.visitDepthFirst(onEach) }
        is DrawCommand.Group -> children.forEach { it.visitDepthFirst(onEach) }
        else -> Unit
    }
}

/** See [visitDepthFirst]. */
fun Iterable<DrawCommand>.visitDepthFirst(onEach: (DrawCommand) -> Unit) {
    for (cmd in this) cmd.visitDepthFirst(onEach)
}
