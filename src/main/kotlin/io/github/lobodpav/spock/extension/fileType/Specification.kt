package io.github.lobodpav.spock.extension.fileType

import io.github.lobodpav.spock.extension.testCreator.dialog.SpecificationParent

object Specification {
    val parent = SpecificationParent("spock.lang.Specification")

    const val CLASS_NAME_SUFFIX = "Spec"
    const val GROOVY_TEST_SOURCE_DIRECTORY_PATH = "src/test/groovy"

    // TODO needed once the GoToTest dialog allows creation of these methods
//    const val SETUP_METHOD_NAME = "setup"
//    const val SETUP_SPEC_METHOD_NAME = "setupSpec"
//
//    const val CLEANUP_METHOD_NAME = "cleanup"
//    const val CLEANUP_SPEC_METHOD_NAME = "cleanupSpec"
}
