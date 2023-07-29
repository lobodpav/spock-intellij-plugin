package io.github.lobodpav.spock.logging

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
}

private class LoggerSample {
    private companion object : Logger()

    @Suppress("unused")
    fun logMe() {
        log.info("A log message")
    }
}
