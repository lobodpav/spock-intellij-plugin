package io.github.lobodpav.spock.test

import com.intellij.openapi.application.ReadAction
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.FirstParam

class PsiElementJvmExtensions {

    /**
     * PSI read access is only allowed from inside a read-action.
     * <p>
     * Therefore, this extension wraps the closure in the
     * {@link ReadAction#compute(com.intellij.openapi.util.ThrowableComputable)}
     * and returns the result.
     * <p>
     * The closure receives the object as <code>this</code> for convenience.
     * <p>
     * <b>Example:</b>
     * <pre>
     * PsiDirectory sourceDirectory = idea.findOrCreateDirectory("test/groovy/dir1/dir2/myDirectory")
     * assert sourceDirectory.read { name } == "myDirectory"
     * </pre>
     */
    static <S, T> T read(
        S self,

        @DelegatesTo(type = "S", strategy = Closure.DELEGATE_FIRST)
        @ClosureParams(FirstParam.class)
        Closure<T> closure
    ) {
        def closureClone = closure.rehydrate(self, this, this)
        closureClone.resolveStrategy = Closure.DELEGATE_FIRST

        ReadAction.compute { closureClone.call(self) }
    }
}
