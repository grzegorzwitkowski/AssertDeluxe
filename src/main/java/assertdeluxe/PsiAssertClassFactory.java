package assertdeluxe;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;

public class PsiAssertClassFactory {

    private AssertClassCodeGenerator codeGenerator;
    private PsiFacade psiFacade;

    public PsiAssertClassFactory(AssertClassCodeGenerator codeGenerator, PsiFacade psiFacade) {
        this.codeGenerator = codeGenerator;
        this.psiFacade = psiFacade;
    }

    public PsiMethod createFieldAssertMethod(PsiClass assertClass, PsiField field) {
        return psiFacade.createMethodFromText(codeGenerator.fieldAssertMethod(field), assertClass);
    }

    public PsiMethod createStaticAssertMethod(PsiClass assertClass) {
        return psiFacade.createMethodFromText(codeGenerator.staticAssertMethod(), assertClass);
    }

    public PsiField createSourceClassField(PsiClass assertClass) {
        return psiFacade.createFieldFromText(codeGenerator.sourceClassField(), assertClass);
    }

    public PsiMethod createConstructor(PsiClass assertClass) {
        return psiFacade.createMethodFromText(codeGenerator.constructor(), assertClass);
    }

    public PsiClass createAssertClass(String sourceClassName) {
        return psiFacade.createClass(createAssertClassName(sourceClassName));
    }

    private String createAssertClassName(String sourceClassName) {
        return sourceClassName + "Assert";
    }
}
