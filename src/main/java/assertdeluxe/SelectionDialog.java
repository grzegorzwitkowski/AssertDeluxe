package assertdeluxe;

import com.intellij.ide.util.DefaultPsiElementCellRenderer;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiField;
import com.intellij.ui.CollectionListModel;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBList;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.List;

import static javax.swing.ListSelectionModel.SINGLE_SELECTION;

public class SelectionDialog extends DialogWrapper {

    private JPanel rootPanel;
    private JBList fieldList;
    private JBList testSourcesRootsList;

    public SelectionDialog(PsiClass psiClass, PsiFacade psiFacade) {
        super(psiClass.getProject());
        setTitle("Select Fields for Assertions");
        rootPanel = new JPanel(new GridBagLayout());
        fieldList = new JBList(new CollectionListModel<>(psiClass.getFields()));
        List<PsiDirectory> testSourcesRoots = psiFacade.getTestSourcesRoots();
        testSourcesRootsList = new JBList(new CollectionListModel<>(testSourcesRoots));
        fieldSelection();
        if (testSourcesRootsList.getItemsCount() == 1) {
            testSourcesRootsList.setSelectedIndex(0);
        } else if (testSourcesRootsList.getItemsCount() > 1) {
            testSourcesRootSelection();
        }

        init();
    }

    private void fieldSelection() {
        fieldList.setCellRenderer(new DefaultPsiElementCellRenderer());
        ToolbarDecorator decorator = ToolbarDecorator.createDecorator(fieldList);
        decorator.disableAddAction();
        decorator.disableRemoveAction();
        LabeledComponent<JPanel> labeledComponent = LabeledComponent.create(decorator.createPanel(), "Fields to include in assertions");
        GridBagConstraints constraints = gridConstraints(0, 0);
        rootPanel.add(labeledComponent, constraints);
    }

    private void testSourcesRootSelection() {
        testSourcesRootsList.setSelectionMode(SINGLE_SELECTION);
        testSourcesRootsList.setCellRenderer(new DefaultPsiElementCellRenderer());
        ToolbarDecorator decorator = ToolbarDecorator.createDecorator(testSourcesRootsList);
        decorator.disableAddAction();
        decorator.disableRemoveAction();
        decorator.disableUpDownActions();
        LabeledComponent<JPanel> labeledComponent = LabeledComponent.create(decorator.createPanel(), "Test sources root for assertion class");
        GridBagConstraints constraints = gridConstraints(0, 1);
        constraints.insets = new Insets(10, 0, 0, 0);
        rootPanel.add(labeledComponent, constraints);
    }

    private GridBagConstraints gridConstraints(int gridx, int gridy) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = gridx;
        constraints.gridy = gridy;
        constraints.weightx = 0.9;
        constraints.weighty = 0.9;
        constraints.fill = GridBagConstraints.BOTH;
        return constraints;
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return rootPanel;
    }

    public List<PsiField> getFields() {
        return fieldList.getSelectedValuesList();
    }

    public PsiDirectory getTestSourcesRoot() {
        return (PsiDirectory) testSourcesRootsList.getSelectedValue();
    }
}
