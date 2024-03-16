package io.github.lobodpav.spock.test.idea

import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.application.WriteAction

/** Invokes a runnable on in a read-action when in need of reading PSI */
fun invokeReadAction(runnable: () -> Unit) {
    ReadAction.run<RuntimeException>(runnable)
}

/** Invokes a supplier on in a read-action when in need of reading PSI, and returns the result */
fun <T> computeReadAction(supplier: () -> T): T =
    ReadAction.compute<T, RuntimeException>(supplier)

/** Invokes a runnable on in a write-action when in need of modifying PSI */
fun invokeWriteAction(runnable: () -> Unit) {
    WriteAction.runAndWait<RuntimeException>(runnable)
}

/** Invokes a supplier on in a write-action when in need of modifying PSI, and returns the result */
fun <T> computeWriteAction(supplier: () -> T): T =
    WriteAction.computeAndWait<T, RuntimeException>(supplier)
