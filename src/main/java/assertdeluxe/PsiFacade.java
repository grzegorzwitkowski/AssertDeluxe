package assertdeluxe;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.util.PsiTreeUtil;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import static com.intellij.openapi.actionSystem.CommonDataKeys.EDITOR;
import static com.intellij.openapi.actionSystem.CommonDataKeys.PSI_FILE;
import static java.io.File.separator;
import static org.jetbrains.jps.model.java.JavaSourceRootType.TEST_SOURCE;

public class PsiFacade {

    private Project project;
    private Module module;

    public PsiFacade(Project project, Module module) {
        this.project = project;
        this.module = module;
    }

    public PsiClass getPsiClassFromEvent(AnActionEvent e) {
        PsiFile file = e.getData(PSI_FILE);
        Editor editor = e.getData(EDITOR);
        if (file == null || editor == null) {
            return null;
        }
        int offset = editor.getCaretModel().getOffset();
        PsiElement element = file.findElementAt(offset);
        return PsiTreeUtil.getParentOfType(element, PsiClass.class);
    }

    public PsiMethod createMethodFromText(String methodText, PsiClass targetClass) {
        return JavaPsiFacade.getElementFactory(project).createMethodFromText(methodText, targetClass);
    }

    public PsiField createFieldFromText(String fieldText, PsiClass targetClass) {
        return JavaPsiFacade.getElementFactory(project).createFieldFromText(fieldText, targetClass);
    }

    public PsiClass createClass(String className) {
        return JavaPsiFacade.getElementFactory(project).createClass(className);
    }

    public PsiDirectory getTestSourcesRoot() {
        VirtualFile root = testSourcesRoots().get(0);
        return PsiManager.getInstance(project).findDirectory(root);
    }

    public List<PsiDirectory> getTestSourcesRoots() {
        List<PsiDirectory> testSourcesRoots = new LinkedList<>();
        for (VirtualFile vf : testSourcesRoots()) {
            testSourcesRoots.add(PsiManager.getInstance(project).findDirectory(vf));
        }
        return testSourcesRoots;
    }

    private List<VirtualFile> testSourcesRoots() {
        return ModuleRootManager.getInstance(module).getSourceRoots(TEST_SOURCE);
    }

    public PsiElement shortenClassReferences(PsiClass psiClass) {
        return JavaCodeStyleManager.getInstance(project).shortenClassReferences(psiClass);
    }

    public PsiElement reformat(PsiClass psiClass) {
        return CodeStyleManager.getInstance(project).reformat(psiClass);
    }

    public void addClassToTargetDir(PsiClass sourceClass, PsiDirectory testSourcesRoot, PsiClass psiClass) throws IOException {
        PsiDirectory targetDir = createTargetDir(sourceClass, testSourcesRoot);
        targetDir.add(psiClass);
    }

    private PsiDirectory createTargetDir(PsiClass sourceClass, PsiDirectory testSourcesRoot) throws IOException {
        String packageName = ((PsiJavaFile) sourceClass.getContainingFile()).getPackageName();
        String packageRelativePath = packageName.replaceAll("\\.", separator);
        VirtualFile directories = VfsUtil.createDirectories(testSourcesRoot.getVirtualFile().getPath() + separator + packageRelativePath);
        return PsiManager.getInstance(project).findDirectory(directories);
    }
}
