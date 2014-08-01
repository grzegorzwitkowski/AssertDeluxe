package assertdeluxe;

import com.intellij.psi.PsiClass;

public class AssertClassCodeGenerator {

    private final PsiClass sourceClass;
    private final String sourceClassFieldName;

    public AssertClassCodeGenerator(PsiClass sourceClass) {
        this.sourceClass = sourceClass;
        this.sourceClassFieldName = createSourceClassFieldName();
    }

    public String sourceClassField() {
        return "private final " + sourceClass.getName() + " " + sourceClassFieldName + ";";
    }

    private String createSourceClassFieldName() {
        return Character.toLowerCase(sourceClass.getName().charAt(0)) + sourceClass.getName().substring(1);
    }
}
