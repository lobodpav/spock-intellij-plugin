package io.github.lobodpav.spock.test

import com.intellij.openapi.application.Application
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.util.Computable
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.FirstParam

class PsiElementJvmExtensions {

    /**
     * Runs a read action and returns the result by wrapping the closure in the
     * {@link Application#runReadAction(Computable)}.
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
