package io.github.lobodpav.spock.test.idea

enum class SourceRoot(val sourceFileType: SourceFileType, val test: Boolean, val path: String) {
    GROOVY_MAIN(SourceFileType.GROOVY, false, "src/main/groovy"),
    GROOVY_TEST(SourceFileType.GROOVY, true, "src/test/groovy"),

    JAVA_MAIN(SourceFileType.JAVA, false, "src/main/java"),
    JAVA_TEST(SourceFileType.JAVA, true, "src/test/java"),

    KOTLIN_MAIN(SourceFileType.KOTLIN, false, "src/main/kotlin"),
    KOTLIN_TEST(SourceFileType.KOTLIN, true, "src/test/kotlin"),
}
