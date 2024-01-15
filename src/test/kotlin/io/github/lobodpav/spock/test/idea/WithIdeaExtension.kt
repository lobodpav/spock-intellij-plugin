package io.github.lobodpav.spock.test.idea

import org.spockframework.runtime.InvalidSpecException
import org.spockframework.runtime.extension.IAnnotationDrivenExtension
import org.spockframework.runtime.model.FieldInfo
import org.spockframework.runtime.model.SpecInfo
import kotlin.reflect.full.isSubclassOf

/**
 * Injects Idea environment into a field within Spock [spock.lang.Specification]
 * if the field is annotated with [WithIdea] annotation.
 *
 * The extension validates correctness of the [WithIdea] annotation within the
 * Spock [spock.lang.Specification].
 */
class WithIdeaExtension : IAnnotationDrivenExtension<WithIdea> {

    override fun visitFieldAnnotation(annotation: WithIdea, field: FieldInfo) {
        if (!field.type.kotlin.isSubclassOf(Idea::class)) {
            throw InvalidSpecException("The '@${WithIdea::class.simpleName}' annotation can only be used on '${Idea::class.qualifiedName}' fields")
        }

        val ideaInterceptor = IdeaInterceptor(field)

        // Attach the interceptor
        val bottomSpec = field.parent.bottomSpec
        when (field.isShared) {
            true -> bottomSpec.addInterceptor(ideaInterceptor)
            else -> bottomSpec.allFeatures.forEach { it.addIterationInterceptor(ideaInterceptor) }
        }
    }

    override fun visitSpec(spec: SpecInfo) {
        val annotations = spec.fields.mapNotNull { it.getAnnotation(WithIdea::class.java) }
        if (annotations.size > 1) {
            throw InvalidSpecException("The '@${WithIdea::class.simpleName}' annotation can only be used once within a specification")
        }
    }
}
