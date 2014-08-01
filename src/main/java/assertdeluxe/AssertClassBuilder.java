package assertdeluxe;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class AssertClassBuilder {

    private Project project;
    private PsiClass sourceClass;
    private List<PsiField> chosenFields;
    private String assertClassName;
    private PsiElementFactory elementFactory;
    private String sourceClassFieldName;
    private AssertClassCodeGenerator assertClassCodeGenerator;

    public AssertClassBuilder(Project project, PsiClass sourceClass, List<PsiField> chosenFields,
                              AssertClassCodeGenerator assertClassCodeGenerator) {
        this.project = project;
        this.sourceClass = sourceClass;
        this.chosenFields = chosenFields;
        this.assertClassName = createAssertClassName(sourceClass);
        this.sourceClassFieldName = createSourceClassFieldName();
        this.elementFactory = JavaPsiFacade.getElementFactory(this.project);
        this.assertClassCodeGenerator = assertClassCodeGenerator;
    }

    public void invoke() throws IOException {

        PsiClass assertClass = elementFactory.createClass(assertClassName);

        PsiField actualField = createSourceClassField(assertClass);
        assertClass.add(actualField);

        StringBuilder constructorText = new StringBuilder()
                .append("private ")
                .append(assertClassName)
                .append("(")
                .append(sourceClass.getName())
                .append(" ")
                .append(sourceClassFieldName)
                .append(") {\n")
                .append("this.")
                .append(sourceClassFieldName)
                .append(" = ")
                .append(sourceClassFieldName)
                .append(";\n")
                .append("}");
        PsiMethod constructor = elementFactory.createMethodFromText(constructorText.toString(), assertClass);
        assertClass.add(constructor);

        StringBuilder mainAssertionText = new StringBuilder()
                .append("public static ")
                .append(assertClassName)
                .append(" assert")
                .append(sourceClass.getName())
                .append("(")
                .append(sourceClass.getName())
                .append(" ")
                .append(sourceClassFieldName)
                .append(") {\n")
                .append("return new ")
                .append(assertClassName)
                .append("(")
                .append(sourceClassFieldName)
                .append(");")
                .append("}");
        PsiMethod mainAssertion = elementFactory.createMethodFromText(mainAssertionText.toString(), assertClass);
        assertClass.add(mainAssertion);

        for (PsiField field : chosenFields) {
            String fieldCC = Character.toUpperCase(field.getName().charAt(0)) + field.getName().substring(1);
            String fieldType = field.getTypeElement().getText();
            String parameterName = field.getName();
            StringBuilder methodText = new StringBuilder()
                    .append("public ")
                    .append(assertClassName)
                    .append(" ")
                    .append("has")
                    .append(fieldCC)
                    .append("(")
                    .append(fieldType)
                    .append(" ")
                    .append(parameterName)
                    .append(") {\n")
                    .append("org.assertj.core.api.Assertions.assertThat(")
                    .append(sourceClassFieldName)
                    .append(".get")
                    .append(fieldCC)
                    .append("()).isEqualTo(")
                    .append(parameterName)
                    .append(");\n")
                    .append("return this;\n")
                    .append("}");
            PsiMethod assertMethod = elementFactory.createMethodFromText(methodText.toString(), assertClass);
            assertClass.add(assertMethod);
        }

        JavaCodeStyleManager.getInstance(project).shortenClassReferences(assertClass);

        VirtualFile baseDir = project.getBaseDir();
        VirtualFile srcTestJava = baseDir.findFileByRelativePath("src/test/java");
        String packageName = ((PsiJavaFile) sourceClass.getContainingFile()).getPackageName();
        String packageRelativePath = packageName.replaceAll("\\.", File.separator);
        VirtualFile directories = VfsUtil.createDirectories(srcTestJava.getPath() + File.separator + packageRelativePath);
        PsiDirectory targetDir = PsiManager.getInstance(project).findDirectory(directories);
        targetDir.add(assertClass);
    }

    private PsiField createSourceClassField(PsiClass assertClass) {
        String text = assertClassCodeGenerator.sourceClassField();
        return elementFactory.createFieldFromText(text, assertClass);
    }

    private String createAssertClassName(PsiClass sourceClass) {
        return sourceClass.getName() + "Assert";
    }

    private String createSourceClassFieldName() {
        return Character.toLowerCase(sourceClass.getName().charAt(0)) + sourceClass.getName().substring(1);
    }
}
