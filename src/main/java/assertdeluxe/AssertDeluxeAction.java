package assertdeluxe;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;

import java.util.List;

import static com.intellij.openapi.actionSystem.CommonDataKeys.PSI_FILE;

public class AssertDeluxeAction extends AnAction {

    public void actionPerformed(AnActionEvent e) {
        PsiFacade psiFacade = new PsiFacade(e.getProject(), findModule(e));
        if (psiFacade.getTestSourcesRoots().size() == 0) {
            Messages.showErrorDialog("No Test Sources Root found.", "Custom Assertion Class");
            return;
        }
        PsiClass sourceClass = psiFacade.getPsiClassFromEvent(e);
        SelectionDialog selectionDialog = new SelectionDialog(sourceClass, psiFacade);
        selectionDialog.show();
        if (selectionDialog.isOK()) {
            List<PsiField> selectedFields = selectionDialog.getFields();
            PsiDirectory selectedTestSourcesRoot = selectionDialog.getTestSourcesRoot();
            if (selectedFields.isEmpty()) {
                Messages.showErrorDialog("No field was selected.", "Custom Assertion Class");
                return;
            } else if (selectedTestSourcesRoot == null) {
                Messages.showErrorDialog("No test sources root was selected.", "Custom Assertion Class");
                return;
            }
            generateAssertionClass(sourceClass, selectedFields, selectedTestSourcesRoot, psiFacade);
        }
    }

    @Override
    public void update(AnActionEvent e) {
        PsiFacade psiFacade = new PsiFacade(e.getProject(), findModule(e));
        PsiClass psiClass = psiFacade.getPsiClassFromEvent(e);
        e.getPresentation().setEnabled(psiClass != null);
    }

    private Module findModule(AnActionEvent e) {
        PsiFile file = e.getData(PSI_FILE);
        return ModuleUtil.findModuleForPsiElement(file);
    }

    private void generateAssertionClass(final PsiClass sourceClass, final List<PsiField> chosenFields, final PsiDirectory testSourcesRoot,
                                        final PsiFacade psiFacade) {
        new WriteCommandAction.Simple(sourceClass.getProject()) {

            @Override
            protected void run() throws Throwable {
                new WriteAssertClassCommand(sourceClass, chosenFields, testSourcesRoot, psiFacade).invoke();
            }
        }.execute();
    }

}
