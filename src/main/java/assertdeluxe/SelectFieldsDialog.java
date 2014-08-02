package assertdeluxe;

import com.intellij.ide.util.DefaultPsiElementCellRenderer;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.ui.CollectionListModel;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBList;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;

public class SelectFieldsDialog extends DialogWrapper {

    private final LabeledComponent<JPanel> labeledComponent;
    private final JBList fieldList;

    public SelectFieldsDialog(PsiClass psiClass) {
        super(psiClass.getProject());
        setTitle("Select Fields for Assertions");
        fieldList = new JBList(new CollectionListModel<>(psiClass.getFields()));
        fieldList.setCellRenderer(new DefaultPsiElementCellRenderer());
        ToolbarDecorator decorator = ToolbarDecorator.createDecorator(fieldList);
        decorator.disableAddAction();
        JPanel panel = decorator.createPanel();
        labeledComponent = LabeledComponent.create(panel, "Fields to include in assertions");
        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return labeledComponent;
    }

    public List<PsiField> getFields() {
        return fieldList.getSelectedValuesList();
    }
}
