package assertdeluxe;

import com.intellij.ide.util.DefaultPsiElementCellRenderer;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.psi.PsiDirectory;
import com.intellij.ui.CollectionListModel;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBList;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;


public class SelectTestSourceRootDialog extends DialogWrapper {

    private LabeledComponent<JPanel> labeledComponent;
    private JBList testSourceRoots;

    public SelectTestSourceRootDialog(Project project, TestSourceRootProvider testSourceRootProvider) {
        super(project);
        setTitle("Select Test Source Root");
        testSourceRoots = new JBList(new CollectionListModel<>(testSourceRootProvider.getTestSourceRoots(project)));
        testSourceRoots.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        testSourceRoots.setCellRenderer(new DefaultPsiElementCellRenderer());
        ToolbarDecorator decorator = ToolbarDecorator.createDecorator(testSourceRoots);
        decorator.disableAddAction();
        decorator.disableRemoveAction();
        JPanel panel = decorator.createPanel();
        labeledComponent = LabeledComponent.create(panel, "Fields to include in assertions");
        init();
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
