package io.github.lobodpav.spock.test.idea

import org.spockframework.runtime.extension.ExtensionAnnotation
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.FIELD

/**
 * Injects Idea environment into a field within Spock [spock.lang.Specification]
 */
@Retention(RUNTIME)
@Target(FIELD)
@ExtensionAnnotation(WithIdeaExtension::class)
annotation class WithIdea
