package assertdeluxe;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class WriteAssertClassCommand {

    private PsiAssertClassFactory assertClassFactory;
    private Project project;
    private PsiClass sourceClass;
    private List<PsiField> chosenFields;
    private PsiDirectory testSourceRoot;

    public WriteAssertClassCommand(PsiClass sourceClass, List<PsiField> chosenFields,
                                   PsiDirectory testSourceRoot, PsiAssertClassFactory assertClassFactory) {
        this.project = sourceClass.getProject();
        this.sourceClass = sourceClass;
        this.chosenFields = chosenFields;
        this.testSourceRoot = testSourceRoot;
        this.assertClassFactory = assertClassFactory;
    }

    public void invoke() throws IOException {
        PsiClass assertClass = assembleAssertClass();
        JavaCodeStyleManager.getInstance(project).shortenClassReferences(assertClass);
        CodeStyleManager.getInstance(project).reformat(assertClass);
        addAssertClassToTargetDir(assertClass);
    }

    private PsiClass assembleAssertClass() {
        PsiClass assertClass = assertClassFactory.createAssertClass(sourceClass.getName());
        assertClass.add(assertClassFactory.createSourceClassField(assertClass));
        assertClass.add(assertClassFactory.createConstructor(assertClass));
        assertClass.add(assertClassFactory.createStaticAssertMethod(assertClass));
        for (PsiField field : chosenFields) {
            assertClass.add(assertClassFactory.createFieldAssertMethod(assertClass, field));
        }
        return assertClass;
    }

    private void addAssertClassToTargetDir(PsiClass assertClass) throws IOException {
        PsiDirectory targetDir = createTargetDir();
        targetDir.add(assertClass);
    }

    private PsiDirectory createTargetDir() throws IOException {
        String packageName = ((PsiJavaFile) sourceClass.getContainingFile()).getPackageName();
        String packageRelativePath = packageName.replaceAll("\\.", File.separator);
        VirtualFile directories = VfsUtil.createDirectories(testSourceRoot.getVirtualFile().getPath() + File.separator + packageRelativePath);
        return PsiManager.getInstance(project).findDirectory(directories);
    }
}
