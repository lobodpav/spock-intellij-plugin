package io.github.lobodpav.spock.logging

import java.lang.invoke.MethodHandles
import com.intellij.openapi.diagnostic.Logger as IntelliJLogger

/**
 * Retrieves IntelliJ Logger instance for the class.
 *
 * Does not print out `$Companion` in the class name.
 *
 * @sample LoggerSample
 * */
abstract class Logger {
    protected val log: IntelliJLogger = IntelliJLogger.getInstance(this.javaClass.enclosingClass ?: this.javaClass)

    companion object {

        /**
         * Obtains logger for a Kotlin file without a class. Uses the fully qualified file name.
         *
         * For example, if retrieved in a `src/main/kotlin/foo/bar/Baz.kt` file, the returned logger will use the `foo.bar.BazKt` name.
         */
        @Suppress("NOTHING_TO_INLINE")
        inline fun getInstance(): IntelliJLogger = IntelliJLogger.getInstance(MethodHandles.lookup().lookupClass())
    }
}

private class LoggerSample {
    private companion object : Logger()

    @Suppress("unused")
    fun logMe() {
        log.info("A log message")
    }
}
