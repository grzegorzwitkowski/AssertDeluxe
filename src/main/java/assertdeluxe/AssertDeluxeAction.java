package assertdeluxe;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiField;

import java.util.List;

public class AssertDeluxeAction extends AnAction {

    public void actionPerformed(AnActionEvent e) {
        PsiFacade psiFacade = new PsiFacade(e.getProject());
        PsiClass sourceClass = psiFacade.getPsiClassFromEvent(e);
        int numberOfTestSourcesRoots = psiFacade.getTestSourcesRoots().size();
        if (numberOfTestSourcesRoots == 0) {
            Messages.showErrorDialog("No Test Sources Root found.", "Custom Assertion");
            return;
        }
        List<PsiField> selectedFields = getFieldsSelectedByUser(sourceClass);
        if (selectedFields == null) {
            return;
        }
        PsiDirectory testSourceRoot = choseTestSourcesRoot(sourceClass, numberOfTestSourcesRoots, psiFacade);
        if (testSourceRoot == null) {
            return;
        }
        generateAssertionClass(sourceClass, selectedFields, testSourceRoot, psiFacade);
    }

    @Override
    public void update(AnActionEvent e) {
        PsiFacade psiFacade = new PsiFacade(e.getProject());
        PsiClass psiClass = psiFacade.getPsiClassFromEvent(e);
        e.getPresentation().setEnabled(psiClass != null);
    }

    private List<PsiField> getFieldsSelectedByUser(PsiClass sourceClass) {
        SelectFieldsDialog selectFieldsDialog = new SelectFieldsDialog(sourceClass);
        selectFieldsDialog.show();
        if (selectFieldsDialog.isOK()) {
            return selectFieldsDialog.getFields();
        }
        return null;
    }

    private PsiDirectory choseTestSourcesRoot(PsiClass sourceClass, int numberOfTestSourcesRoots, PsiFacade psiFacade) {
        PsiDirectory testSourceRoot = null;
        if (numberOfTestSourcesRoots == 1) {
            testSourceRoot = psiFacade.getTestSourcesRoot();
        } else if (numberOfTestSourcesRoots > 1) {
            testSourceRoot = getTestSourceRootSelectedByUser(sourceClass, psiFacade);
        }
        return testSourceRoot;
    }

    private PsiDirectory getTestSourceRootSelectedByUser(PsiClass sourceClass, PsiFacade psiFacade) {
        SelectTestSourceRootDialog selectTestSourceRootDialog = new SelectTestSourceRootDialog(sourceClass.getProject(), psiFacade);
        selectTestSourceRootDialog.show();
        if (selectTestSourceRootDialog.isOK()) {
            return selectTestSourceRootDialog.getTestSourceRoot();
        }
        return null;
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
