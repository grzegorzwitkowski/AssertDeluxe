package assertdeluxe;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiField;

import java.io.IOException;
import java.util.List;

public class WriteAssertClassCommand {

    private PsiAssertClassFactory assertClassFactory;
    private PsiClass sourceClass;
    private List<PsiField> chosenFields;
    private PsiDirectory testSourceRoot;
    private PsiFacade psiFacade;

    public WriteAssertClassCommand(PsiClass sourceClass, List<PsiField> chosenFields, PsiDirectory testSourceRoot, PsiFacade psiFacade) {
        this.sourceClass = sourceClass;
        this.chosenFields = chosenFields;
        this.testSourceRoot = testSourceRoot;
        this.assertClassFactory = new PsiAssertClassFactory(new AssertClassCodeGenerator(sourceClass), psiFacade);
        this.psiFacade = psiFacade;
    }

    public void invoke() throws IOException {
        PsiClass assertClass = assembleAssertClass();
        psiFacade.shortenClassReferences(assertClass);
        psiFacade.reformat(assertClass);
        psiFacade.addClassToTargetDir(sourceClass, testSourceRoot, assertClass);
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

}
