package io.github.lobodpav.spock.template

enum class CustomisableTemplateParameter {
    SUPER_CLASS_NAME,
    FQ_SUPER_CLASS_NAME;

    companion object {
        val parameterNames: List<String>
            get() = entries.map { it.name }

        fun parameterMap(fqSuperClassName: String): Map<String, String> {
            return mapOf(
                SUPER_CLASS_NAME.name to fqSuperClassName.substringAfterLast('.'),
                FQ_SUPER_CLASS_NAME.name to fqSuperClassName,
            )
        }
    }
}
