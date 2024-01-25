package io.github.lobodpav.spock.test.extension

class StringJvmExtensions {

    static Tuple decomposeFqClassName(String fqClassName) {
        def lastDotIndex = fqClassName.lastIndexOf(".")
        if (lastDotIndex < 0) return Tuple.tuple(fqClassName, fqClassName)

        def className = fqClassName.substring(lastDotIndex + 1)
        def packageName = fqClassName.substring(0, lastDotIndex)

        return Tuple.tuple(packageName, className)
    }
}
