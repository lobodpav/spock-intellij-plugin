package io.github.lobodpav.spock.test.provider

import com.intellij.ide.IdeView
import com.intellij.psi.PsiDirectory
import io.github.lobodpav.spock.test.idea.Idea

class IdeViewProvider {

    static Map defaults() {
        [
            /** Either an empty string for the `src/` source root directory, or a path to a directory inside the `src/` */
            relativeDirectoryPath: "",
        ]
    }

    /**
     * Creates an {@link IdeView} instance with the single provided directory in the project's virtual file system.
     * The directory is created if it doesn't exist yet.
     */
    static IdeView make(Idea idea, Map overrides = [:]) {
        def props = defaults() + overrides

        def psiDirectory = idea.findOrCreateDirectory(props.relativeDirectoryPath as String)

        return new IdeView() {
            @Override PsiDirectory[] getDirectories() { [psiDirectory] }
            @Override PsiDirectory getOrChooseDirectory() { psiDirectory }
        }
    }
}
