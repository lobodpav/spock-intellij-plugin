package io.github.lobodpav.spock.logging

import kotlin.reflect.KClass
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
        fun getInstance(name: String): IntelliJLogger = IntelliJLogger.getInstance(name)
        fun getInstance(kClass: KClass<*>): IntelliJLogger = IntelliJLogger.getInstance(kClass.java)
    }
}

private class LoggerSample {
    private companion object : Logger()

    @Suppress("unused")
    fun logMe() {
        log.info("A log message")
    }
}
