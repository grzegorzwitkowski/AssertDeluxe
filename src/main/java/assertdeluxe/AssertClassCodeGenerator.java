package assertdeluxe;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;

public class AssertClassCodeGenerator {

    private final PsiClass sourceClass;
    private final String sourceClassFieldName;
    private final String assertClassName;

    public AssertClassCodeGenerator(PsiClass sourceClass) {
        this.sourceClass = sourceClass;
        this.sourceClassFieldName = createSourceClassFieldName();
        this.assertClassName = createAssertClassName(sourceClass);
    }

    public String sourceClassField() {
        return "private final " + sourceClass.getName() + " " + sourceClassFieldName + ";";
    }

    public String constructor() {
        return "private " + assertClassName + "(" + sourceClass.getName() + " " + sourceClassFieldName
                + ") {this." + sourceClassFieldName + " = " + sourceClassFieldName + ";" + "}";
    }

    private String createSourceClassFieldName() {
        return Character.toLowerCase(sourceClass.getName().charAt(0)) + sourceClass.getName().substring(1);
    }

    private String createAssertClassName(PsiClass sourceClass) {
        return sourceClass.getName() + "Assert";
    }

    public String staticAssertMethod() {
        return "public static " + assertClassName + " assert" + sourceClass.getName() + "(" + sourceClass.getName() + " "
                + sourceClassFieldName + ") {return new " + assertClassName + "(" + sourceClassFieldName + ");}";
    }

    public String fieldAssertMethod(PsiField field) {
        String fieldCC = Character.toUpperCase(field.getName().charAt(0)) + field.getName().substring(1);
        String fieldType = field.getTypeElement().getText();
        return "public " + assertClassName + " " + "has" + fieldCC + "(" + fieldType + " " + field.getName()
                + ") {org.assertj.core.api.Assertions.assertThat(" + sourceClassFieldName + ".get" + fieldCC + "()).isEqualTo("
                + field.getName() + ");return this;}";
    }
}
