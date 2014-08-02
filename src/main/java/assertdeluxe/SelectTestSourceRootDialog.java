package assertdeluxe;

import com.intellij.ide.util.DefaultPsiElementCellRenderer;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiManager;
import com.intellij.ui.CollectionListModel;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBList;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.java.JavaModuleSourceRootTypes;

import javax.swing.*;
import java.util.List;
import java.util.stream.Collectors;


public class SelectTestSourceRootDialog extends DialogWrapper {

    private final LabeledComponent<JPanel> labeledComponent;
    private final JBList testSourceRoots;

    public SelectTestSourceRootDialog(PsiClass sourceClass) {
        super(sourceClass.getProject());
        setTitle("Select Test Source Root");
        testSourceRoots = new JBList(new CollectionListModel<>(testSourceRoots(sourceClass)));
        testSourceRoots.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        testSourceRoots.setCellRenderer(new DefaultPsiElementCellRenderer());
        ToolbarDecorator decorator = ToolbarDecorator.createDecorator(testSourceRoots);
        decorator.disableAddAction();
        decorator.disableRemoveAction();
        JPanel panel = decorator.createPanel();
        labeledComponent = LabeledComponent.create(panel, "Fields to include in assertions");
        init();
    }

    private List<PsiDirectory> testSourceRoots(PsiClass sourceClass) {
        ProjectRootManager projectRootManager = ProjectRootManager.getInstance(sourceClass.getProject());
        List<VirtualFile> roots = projectRootManager.getModuleSourceRoots(JavaModuleSourceRootTypes.TESTS);
        PsiManager psiManager = PsiManager.getInstance(sourceClass.getProject());
        return roots.stream().map(psiManager::findDirectory).collect(Collectors.toList());
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return labeledComponent;
    }

    public PsiDirectory getTestSourceRoot() {
        return (PsiDirectory) testSourceRoots.getSelectedValue();
    }
}
