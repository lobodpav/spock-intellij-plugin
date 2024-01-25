package io.github.lobodpav.spock.extension.testCreator.dialog

import com.intellij.openapi.vfs.VirtualFile

data class CreateSpecificationDialogOutput(
    /** Example: `MyCoolSpec` */
    val className: String,

    /** Example: `module1/src/test/groovy/` */
    val groovyTestSourceRoot: VirtualFile,

    /** Example: `foo.bar.baz` */
    val destinationPackage: String,

    /** Example: `spock.lang.Specification` */
    val fqSuperClassName: String,
)
