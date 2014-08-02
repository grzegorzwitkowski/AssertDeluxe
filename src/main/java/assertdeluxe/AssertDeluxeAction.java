package assertdeluxe;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;

import java.util.List;

public class AssertDeluxeAction extends AnAction {

    private final TestSourceRootProvider testSourceRootProvider;

    public AssertDeluxeAction() {
        testSourceRootProvider = new TestSourceRootProvider();
    }

    public void actionPerformed(AnActionEvent e) {
        PsiClass sourceClass = getPsiClassFromEvent(e);
        int numberOfTestSourcesRoots = testSourceRootProvider.getTestSourceRoots(sourceClass.getProject()).size();
        if (numberOfTestSourcesRoots == 0) {
            Messages.showErrorDialog("No Test Sources Root found.", "Custom Assertion");
            return;
        }
        List<PsiField> selectedFields = getFieldsSelectedByUser(sourceClass);
        if (selectedFields == null) {
            return;
        }
        PsiDirectory testSourceRoot = choseTestSourcesRoot(sourceClass, numberOfTestSourcesRoots);
        if (testSourceRoot == null) {
            return;
        }
        generateAssertionClass(sourceClass, selectedFields, testSourceRoot);
    }

    @Override
    public void update(AnActionEvent e) {
        PsiClass psiClass = getPsiClassFromEvent(e);
        e.getPresentation().setEnabled(psiClass != null);
    }

    private PsiClass getPsiClassFromEvent(AnActionEvent e) {
        PsiFile file = e.getData(LangDataKeys.PSI_FILE);
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        if (file == null || editor == null) {
            return null;
        }
        int offset = editor.getCaretModel().getOffset();
        PsiElement element = file.findElementAt(offset);
        return PsiTreeUtil.getParentOfType(element, PsiClass.class);
    }

    private List<PsiField> getFieldsSelectedByUser(PsiClass sourceClass) {
        SelectFieldsDialog selectFieldsDialog = new SelectFieldsDialog(sourceClass);
        selectFieldsDialog.show();
        if (selectFieldsDialog.isOK()) {
            return selectFieldsDialog.getFields();
        }
        return null;
    }

    private PsiDirectory choseTestSourcesRoot(PsiClass sourceClass, int numberOfTestSourcesRoots) {
        PsiDirectory testSourceRoot = null;
        if (numberOfTestSourcesRoots == 1) {
            testSourceRoot = testSourceRootProvider.getTestSourcesRoot(sourceClass.getProject());
        } else if (numberOfTestSourcesRoots > 1) {
            testSourceRoot = getTestSourceRootSelectedByUser(sourceClass);
        }
        return testSourceRoot;
    }

    private PsiDirectory getTestSourceRootSelectedByUser(PsiClass sourceClass) {
        SelectTestSourceRootDialog selectTestSourceRootDialog = new SelectTestSourceRootDialog(sourceClass.getProject(), testSourceRootProvider);
        selectTestSourceRootDialog.show();
        if (selectTestSourceRootDialog.isOK()) {
            return selectTestSourceRootDialog.getTestSourceRoot();
        }
        return null;
    }

    private void generateAssertionClass(PsiClass sourceClass, List<PsiField> chosenFields, PsiDirectory testSourceRoot) {
        new WriteCommandAction.Simple(sourceClass.getProject()) {

            @Override
            protected void run() throws Throwable {
                AssertClassCodeGenerator assertClassCodeGenerator = new AssertClassCodeGenerator(sourceClass);
                PsiElementFactory psiElementFactory = JavaPsiFacade.getElementFactory(getProject());
                PsiAssertClassFactory psiAssertClassFactory = new PsiAssertClassFactory(assertClassCodeGenerator, psiElementFactory);
                new WriteAssertClassCommand(sourceClass, chosenFields, testSourceRoot, psiAssertClassFactory).invoke();
            }
        }.execute();
    }
}
