import assertdeluxe.AssertClassBuilder;
import assertdeluxe.AssertClassCodeGenerator;
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
        SelectFieldsDialog selectFieldsDialog = new SelectFieldsDialog(sourceClass);
        selectFieldsDialog.show();
        if (selectFieldsDialog.isOK()) {
            generateAssertionClass(sourceClass, selectFieldsDialog.getFields());
        }
    }

    @Override
    public void update(AnActionEvent e) {
        PsiClass psiClass = getPsiClassFromEvent(e);
        e.getPresentation().setEnabled(psiClass != null);
    }

    private void generateAssertionClass(PsiClass sourceClass, List<PsiField> chosenFields) {

        new WriteCommandAction.Simple(sourceClass.getProject()) {

            @Override
            protected void run() throws Throwable {
                new AssertClassBuilder(getProject(), sourceClass, chosenFields, new AssertClassCodeGenerator(sourceClass)).invoke();
            }
        }.execute();
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

}
