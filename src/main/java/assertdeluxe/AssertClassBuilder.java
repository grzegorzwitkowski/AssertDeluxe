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
import com.intellij.psi.codeStyle.CodeStyleManager;
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
    private AssertClassCodeGenerator codeGenerator;

    public AssertClassBuilder(Project project, PsiClass sourceClass, List<PsiField> chosenFields,
                              AssertClassCodeGenerator codeGenerator) {
        this.project = project;
        this.sourceClass = sourceClass;
        this.chosenFields = chosenFields;
        this.assertClassName = createAssertClassName(sourceClass);
        this.elementFactory = JavaPsiFacade.getElementFactory(this.project);
        this.codeGenerator = codeGenerator;
    }

    public void invoke() throws IOException {
        PsiClass assertClass = elementFactory.createClass(assertClassName);
        assertClass.add(createSourceClassField(assertClass));
        assertClass.add(createConstructor(assertClass));
        assertClass.add(createStaticAssertMethod(assertClass));
        for (PsiField field : chosenFields) {
            assertClass.add(createFieldAssertMethod(assertClass, field));
        }

        JavaCodeStyleManager.getInstance(project).shortenClassReferences(assertClass);
        CodeStyleManager.getInstance(project).reformat(assertClass);

        VirtualFile baseDir = project.getBaseDir();
        VirtualFile srcTestJava = baseDir.findFileByRelativePath("src/test/java");
        String packageName = ((PsiJavaFile) sourceClass.getContainingFile()).getPackageName();
        String packageRelativePath = packageName.replaceAll("\\.", File.separator);
        VirtualFile directories = VfsUtil.createDirectories(srcTestJava.getPath() + File.separator + packageRelativePath);
        PsiDirectory targetDir = PsiManager.getInstance(project).findDirectory(directories);
        targetDir.add(assertClass);
    }

    private PsiMethod createFieldAssertMethod(PsiClass assertClass, PsiField field) {
        return elementFactory.createMethodFromText(codeGenerator.fieldAssertMethod(field), assertClass);
    }

    private PsiMethod createStaticAssertMethod(PsiClass assertClass) {
        return elementFactory.createMethodFromText(codeGenerator.staticAssertMethod(), assertClass);
    }

    private PsiField createSourceClassField(PsiClass assertClass) {
        return elementFactory.createFieldFromText(codeGenerator.sourceClassField(), assertClass);
    }

    private PsiMethod createConstructor(PsiClass assertClass) {
        return elementFactory.createMethodFromText(codeGenerator.constructor(), assertClass);
    }

    private String createAssertClassName(PsiClass sourceClass) {
        return sourceClass.getName() + "Assert";
    }
}
