package io.github.lobodpav.spock.extension.testCreator

import com.intellij.openapi.application.WriteAction
import com.intellij.psi.PsiDirectory
import com.intellij.util.IncorrectOperationException

/**
 * Creates directory structure for a given package name.
 *
 * The [com.intellij.ide.util.PackageUtil.findOrCreateDirectoryForPackage] is not suitable for this plugin
 * because if there are multiple test source roots (e.g. `src/test/groovy` and `kotlin`), a directory chooser dialog would get opened.
 * This is not desired because the user has already selected a particular module with an existing `groovy` test source root to create the Specification in.
 *
 * @throws com.intellij.util.IncorrectOperationException
 */
fun PsiDirectory.createPackage(packageName: String): PsiDirectory =
    WriteAction.compute<PsiDirectory, IncorrectOperationException> {
        var currentDirectory = this

        packageName.splitToSequence(".").forEach {
            currentDirectory = currentDirectory.findSubdirectory(it) ?: currentDirectory.createSubdirectory(it)
        }

        currentDirectory
    }
