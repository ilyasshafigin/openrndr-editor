package ru.ilyasshafigin.openrndr.editor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import org.openrndr.launch
import kotlin.coroutines.CoroutineContext

fun Editor<*>.launch(
    context: CoroutineContext = program.dispatcher,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> Unit
): Job = program.launch(context, start, block)
