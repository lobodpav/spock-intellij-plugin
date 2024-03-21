package io.github.lobodpav.spock.test.idea

import io.github.lobodpav.spock.logging.Logger
import org.spockframework.runtime.extension.IMethodInterceptor
import org.spockframework.runtime.extension.IMethodInvocation
import org.spockframework.runtime.model.FieldInfo

/**
 * Intercepts feature methods and injects a fresh Idea environment
 * into a field annotated with [WithIdea] within Spock [spock.lang.Specification].
 *
 * The interceptor does not perform any validation.
 */
class IdeaInterceptor(
    private val fieldInfo: FieldInfo,
) : IMethodInterceptor {

    private companion object : Logger()

    override fun intercept(invocation: IMethodInvocation) {
        log.info("Setting up '${SpockCodeInsightFixtureTestCase::class.simpleName}'")
        val spockCodeInsightFixtureTestCase = SpockCodeInsightFixtureTestCase().apply {
            setup()
        }

        val idea = Idea(spockCodeInsightFixtureTestCase.fixture)
        fieldInfo.writeValue(invocation.instance, idea)

        try {
            invocation.proceed()
        } finally {
            log.info("Cleaning up '${SpockCodeInsightFixtureTestCase::class.simpleName}'")
            spockCodeInsightFixtureTestCase.cleanup()
            fieldInfo.writeValue(invocation.instance, null)
        }
    }
}
