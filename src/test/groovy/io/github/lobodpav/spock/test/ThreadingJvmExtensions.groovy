package io.github.lobodpav.spock.test

import com.intellij.openapi.application.ReadAction
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.FirstParam

import static io.github.lobodpav.spock.test.idea.ThreadingKt.computeReadAction

class ThreadingJvmExtensions {

    /**
     * PSI read access is only allowed from inside a read-action.
     * <p>
     * Therefore, this extension wraps the closure in the
     * {@link com.intellij.openapi.application.ReadAction#compute(com.intellij.openapi.util.ThrowableComputable)}
     * and returns the result.
     * <p>
     * The closure receives the object as <code>this</code> for convenience.
     * <p>
     * <b>Example:</b>
     * <pre>
     * PsiDirectory sourceDirectory = idea.findOrCreateDirectory("src/test/groovy/dir1/dir2/myDirectory")
     * assert sourceDirectory.read { name } == "myDirectory"
     * </pre>
     * <b>Note:</b> The closure cannot reference variables outside its scope. For this purpose, use
     * {@link ThreadingJvmExtensions#readIt(java.lang.Object, groovy.lang.Closure)}.
     */
    static <S, T> T read(
        S self,

        @DelegatesTo(type = "S", strategy = Closure.DELEGATE_FIRST)
        @ClosureParams(FirstParam.class)
        Closure<T> closure
    ) {
        def closureClone = closure.rehydrate(self, this, this)
        closureClone.resolveStrategy = Closure.DELEGATE_FIRST

        computeReadAction { closureClone.call(self) }
    }

    /**
     * The same as {@link ThreadingJvmExtensions#read(java.lang.Object, groovy.lang.Closure)}
     * except the receiver closure parameter is <code>it</code> rather than <code>this</code>.
     * <p>
     * Useful when the closure needs to references variables outside the closure.
     */
    static <S, T> T readIt(S self, @ClosureParams(FirstParam.class) Closure<T> closure) {
        ReadAction.compute { closure.call(self) }
    }
}
