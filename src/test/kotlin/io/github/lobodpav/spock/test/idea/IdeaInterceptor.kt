package io.github.lobodpav.spock.test.idea

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


    override fun intercept(invocation: IMethodInvocation) {
        val spockCodeInsightFixtureTestCase = SpockCodeInsightFixtureTestCase().apply {
            setup()
        }

        val idea = Idea(spockCodeInsightFixtureTestCase.fixture)
        fieldInfo.writeValue(invocation.instance, idea)

        try {
            invocation.proceed()
        } finally {
            // Clean up
            spockCodeInsightFixtureTestCase.cleanup()
            fieldInfo.writeValue(invocation.instance, null)
        }
    }
}
