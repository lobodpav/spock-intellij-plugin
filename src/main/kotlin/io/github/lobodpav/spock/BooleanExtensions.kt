package io.github.lobodpav.spock

fun Boolean.ifTrue(block: () -> Unit): Boolean {
    if (this) { block() }
    return this
}

fun Boolean.ifFalse(block: () -> Unit): Boolean {
    if (!this) { block() }
    return this
}
