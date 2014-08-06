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
        PsiDirectory testSourcesRoot = choseTestSourcesRoot(sourceClass, numberOfTestSourcesRoots, psiFacade);
        if (testSourcesRoot == null) {
            return;
        }
        generateAssertionClass(sourceClass, selectedFields, testSourcesRoot, psiFacade);
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

    private List<PsiField> getFieldsSelectedByUser(PsiClass sourceClass) {
        SelectFieldsDialog selectFieldsDialog = new SelectFieldsDialog(sourceClass);
        selectFieldsDialog.show();
        if (selectFieldsDialog.isOK()) {
            return selectFieldsDialog.getFields();
        }
        return null;
    }

    private PsiDirectory choseTestSourcesRoot(PsiClass sourceClass, int numberOfTestSourcesRoots, PsiFacade psiFacade) {
        PsiDirectory testSourcesRoot = null;
        if (numberOfTestSourcesRoots == 1) {
            testSourcesRoot = psiFacade.getTestSourcesRoot();
        } else if (numberOfTestSourcesRoots > 1) {
            testSourcesRoot = getTestSourcesRootSelectedByUser(sourceClass, psiFacade);
        }
        return testSourcesRoot;
    }

    private PsiDirectory getTestSourcesRootSelectedByUser(PsiClass sourceClass, PsiFacade psiFacade) {
        SelectTestSourcesRootDialog selectTestSourcesRootDialog = new SelectTestSourcesRootDialog(sourceClass.getProject(), psiFacade);
        selectTestSourcesRootDialog.show();
        if (selectTestSourcesRootDialog.isOK()) {
            return selectTestSourcesRootDialog.getTestSourcesRoot();
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
