package assertdeluxe;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;

import java.util.List;

public class AssertDeluxeAction extends AnAction {

    public void actionPerformed(AnActionEvent e) {
        PsiClass sourceClass = getPsiClassFromEvent(e);
        List<PsiField> selectedFields = getFieldsSelectedByUser(sourceClass);
        if (selectedFields == null) {
            return;
        }
        PsiDirectory testSourceRoot = getTestSourceRootSelectedByUser(sourceClass);
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

    private PsiDirectory getTestSourceRootSelectedByUser(PsiClass sourceClass) {
        SelectTestSourceRootDialog selectTestSourceRootDialog = new SelectTestSourceRootDialog(sourceClass);
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
