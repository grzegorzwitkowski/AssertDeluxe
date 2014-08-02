package assertdeluxe;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;

public class PsiAssertClassFactory {

    private AssertClassCodeGenerator codeGenerator;
    private PsiElementFactory elementFactory;

    public PsiAssertClassFactory(AssertClassCodeGenerator codeGenerator, PsiElementFactory elementFactory) {
        this.codeGenerator = codeGenerator;
        this.elementFactory = elementFactory;
    }

    public PsiMethod createFieldAssertMethod(PsiClass assertClass, PsiField field) {
        return elementFactory.createMethodFromText(codeGenerator.fieldAssertMethod(field), assertClass);
    }

    public PsiMethod createStaticAssertMethod(PsiClass assertClass) {
        return elementFactory.createMethodFromText(codeGenerator.staticAssertMethod(), assertClass);
    }

    public PsiField createSourceClassField(PsiClass assertClass) {
        return elementFactory.createFieldFromText(codeGenerator.sourceClassField(), assertClass);
    }

    public PsiMethod createConstructor(PsiClass assertClass) {
        return elementFactory.createMethodFromText(codeGenerator.constructor(), assertClass);
    }

    public PsiClass createAssertClass(String sourceClassName) {

        return elementFactory.createClass(createAssertClassName(sourceClassName));
    }

    private String createAssertClassName(String sourceClassName) {
        return sourceClassName + "Assert";
    }
}
