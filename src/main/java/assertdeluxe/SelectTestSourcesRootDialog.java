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

import static javax.swing.ListSelectionModel.SINGLE_SELECTION;


public class SelectTestSourcesRootDialog extends DialogWrapper {

    private LabeledComponent<JPanel> labeledComponent;
    private JBList testSourcesRoots;

    public SelectTestSourcesRootDialog(Project project, PsiFacade psiFacade) {
        super(project);
        setTitle("Select Test Sources Root");
        testSourcesRoots = new JBList(new CollectionListModel<>(psiFacade.getTestSourcesRoots()));
        testSourcesRoots.setSelectionMode(SINGLE_SELECTION);
        testSourcesRoots.setCellRenderer(new DefaultPsiElementCellRenderer());
        ToolbarDecorator decorator = ToolbarDecorator.createDecorator(testSourcesRoots);
        decorator.disableAddAction();
        decorator.disableRemoveAction();
        JPanel panel = decorator.createPanel();
        labeledComponent = LabeledComponent.create(panel, "Test sources root for assertion class");
        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return labeledComponent;
    }

    public PsiDirectory getTestSourcesRoot() {
        return (PsiDirectory) testSourcesRoots.getSelectedValue();
    }
}
