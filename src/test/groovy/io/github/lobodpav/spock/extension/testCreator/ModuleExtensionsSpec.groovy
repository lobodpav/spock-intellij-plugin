package io.github.lobodpav.spock.extension.testCreator

import com.intellij.openapi.module.Module
import com.intellij.openapi.roots.DependencyScope
import com.intellij.openapi.roots.ModuleRootModificationUtil
import com.intellij.psi.PsiFile
import io.github.lobodpav.spock.test.idea.Idea
import io.github.lobodpav.spock.test.idea.SourceRoot
import io.github.lobodpav.spock.test.idea.TestModule
import io.github.lobodpav.spock.test.idea.WithIdea
import spock.lang.Specification

import static io.github.lobodpav.spock.test.idea.SourceRoot.GROOVY_MAIN
import static io.github.lobodpav.spock.test.idea.SourceRoot.GROOVY_TEST
import static io.github.lobodpav.spock.test.idea.SourceRoot.JAVA_MAIN
import static io.github.lobodpav.spock.test.idea.SourceRoot.JAVA_TEST
import static io.github.lobodpav.spock.test.idea.SourceRoot.KOTLIN_MAIN
import static io.github.lobodpav.spock.test.idea.SourceRoot.KOTLIN_TEST

class ModuleExtensionsSpec extends Specification {

    @WithIdea
    Idea idea

    def "Finds all abstract inheritors of spock.lang.Specification"() {
        given: "The module2 depends on module1 that depends on the root module"
        def rootModule = idea.getModule(TestModule.ROOT)
        def module1 = idea.getModule(TestModule.MODULE1)
        def module2 = idea.getModule(TestModule.MODULE2)
        ModuleRootModificationUtil.addDependency(module1, rootModule, DependencyScope.COMPILE, true)
        ModuleRootModificationUtil.addDependency(module2, module1, DependencyScope.COMPILE, true)

        and: "Groovy Spec classes are created"
        createGroovySpec("foo.GroovyMainSpec1", true, TestModule.ROOT, GROOVY_MAIN)
        createGroovySpec("foo.GroovyMainSpec2", false, TestModule.ROOT, GROOVY_MAIN)
        createGroovySpec("foo.GroovyTestSpec1", true, TestModule.ROOT, GROOVY_TEST)
        createGroovySpec("foo.GroovyTestSpec2", false, TestModule.ROOT, GROOVY_TEST)

        and: "Java Spec classes are created"
        createJavaSpec("foo.JavaMainSpec1", true, TestModule.MODULE1, JAVA_MAIN)
        createJavaSpec("foo.JavaMainSpec2", false, TestModule.MODULE1, JAVA_MAIN)
        createJavaSpec("foo.JavaTestSpec1", true, TestModule.MODULE1, JAVA_TEST)
        createJavaSpec("foo.JavaTestSpec2", false, TestModule.MODULE1, JAVA_TEST)

        and: "Kotlin Spec classes are created"
        createKotlinSpec("foo.KotlinMainSpec1", true, TestModule.MODULE2, KOTLIN_MAIN)
        createKotlinSpec("foo.KotlinMainSpec2", false, TestModule.MODULE2, KOTLIN_MAIN)
        createKotlinSpec("foo.KotlinTestSpec1", true, TestModule.MODULE2, KOTLIN_TEST)
        createKotlinSpec("foo.KotlinTestSpec2", false, TestModule.MODULE2, KOTLIN_TEST)

        expect: "The root module has only got 2 Groovy abstract inheritors of spock.lang.Specification"
        getInheritors(rootModule) == ["foo.GroovyMainSpec1", "foo.GroovyTestSpec1"] as Set

        and: "The module1 has got 2 Java abstract inheritors as well as 2 Groovy ones from dependencies"
        getInheritors(module1) == ["foo.GroovyMainSpec1", "foo.GroovyTestSpec1", "foo.JavaMainSpec1", "foo.JavaTestSpec1"] as Set

        and: "The module2 has got 2 Kotlin abstract inheritors as well as 4 Groovy/Java ones from dependencies"
        getInheritors(module2) == ["foo.GroovyMainSpec1", "foo.GroovyTestSpec1", "foo.JavaMainSpec1", "foo.JavaTestSpec1", "foo.KotlinMainSpec1", "foo.KotlinTestSpec1"] as Set
    }

    private Set<String> getInheritors(Module module) {
        readIt { ModuleExtensionsKt.findAllAbstractSpecificationInheritors(module).collect { it.fqClassName } }
    }

    private PsiFile createGroovySpec(String fqClassName, boolean isAbstract, TestModule testModule, SourceRoot sourceRoot) {
        def fqClass = fqClassName.decomposeFqClassName()

        def spec = """
            package ${fqClass.v1}
            import spock.lang.Specification
            ${isAbstract ? "abstract " : ""}class ${fqClass.v2} extends Specification {}
        """.stripIndent()

        return idea.loadFileContent(fqClassName, spec, testModule, sourceRoot)
    }

    private PsiFile createJavaSpec(String fqClassName, boolean isAbstract, TestModule testModule, SourceRoot sourceRoot) {
        def fqClass = fqClassName.decomposeFqClassName()

        def spec = """
            package ${fqClass.v1};
            import spock.lang.Specification;
            ${isAbstract ? "abstract " : ""}class ${fqClass.v2} extends Specification {}
        """.stripIndent()

        return idea.loadFileContent(fqClassName, spec, testModule, sourceRoot)
    }

    private PsiFile createKotlinSpec(String fqClassName, boolean isAbstract, TestModule testModule, SourceRoot sourceRoot) {
        def fqClass = fqClassName.decomposeFqClassName()

        def spec = """
            package ${fqClass.v1}
            import spock.lang.Specification
            ${isAbstract ? "abstract " : ""}class ${fqClass.v2} : Specification
        """.stripIndent()

        return idea.loadFileContent(fqClassName, spec, testModule, sourceRoot)
    }
}
