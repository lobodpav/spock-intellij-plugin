package io.github.lobodpav.spock.test.idea

import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.application.WriteAction
import com.intellij.util.concurrency.AppExecutorUtil

/** Invokes a runnable on in a read-action when in need of reading PSI */
fun invokeReadAction(runnable: () -> Unit) {
    ReadAction.run<RuntimeException>(runnable)
}

/**
 * Invokes a callable on in a read-action on a background thread and returns the result to a consumer on the UI thread.
 *
 * Useful when calling slow operations on UI threads to avoid `Slow operations are prohibited on EDT` exceptions.
 */
fun <T> invokeReadActionInBackgroundThread(backgroundThreadCallable: () -> T, uiThreadConsumer: (T) -> Unit): T =
    ReadAction.nonBlocking(backgroundThreadCallable)
        .finishOnUiThread(ModalityState.defaultModalityState(), uiThreadConsumer)
        .submit(AppExecutorUtil.getAppExecutorService())
        .get()

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
