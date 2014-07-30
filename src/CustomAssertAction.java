import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.impl.file.PsiDirectoryFactory;
import com.intellij.psi.util.PsiTreeUtil;

import java.io.File;
import java.util.List;

public class CustomAssertAction extends AnAction {

    public void actionPerformed(AnActionEvent e) {
        PsiClass psiClass = getPsiClassFromEvent(e);
        SelectFieldsDialog selectFieldsDialog = new SelectFieldsDialog(psiClass);
        selectFieldsDialog.show();
        if (selectFieldsDialog.isOK()) {
            generateAssertionClass(psiClass, selectFieldsDialog.getFields());
        }
    }

    private void generateAssertionClass(PsiClass psiClass, List<PsiField> fields) {

        String assertClassName = psiClass.getName() + "Assert";

        PsiFileFactory fileFactory = PsiFileFactory.getInstance(psiClass.getProject());
        PsiFile assertClassFile = fileFactory.createFileFromText(assertClassName + ".java", JavaLanguage.INSTANCE, "");

        new WriteCommandAction.Simple(psiClass.getProject(), assertClassFile) {

            @Override
            protected void run() throws Throwable {
                PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(getProject());
                PsiClass assertClass = elementFactory.createClass(assertClassName);
                String actualFieldName = Character.toLowerCase(psiClass.getName().charAt(0)) + psiClass.getName().substring(1);

                StringBuilder fieldText = new StringBuilder()
                        .append("private final ")
                        .append(psiClass.getName())
                        .append(" ")
                        .append(actualFieldName)
                        .append(";");
                PsiField actualField = elementFactory.createFieldFromText(fieldText.toString(), assertClass);
                assertClass.add(actualField);

                StringBuilder constructorText = new StringBuilder()
                        .append("private ")
                        .append(assertClassName)
                        .append("(")
                        .append(psiClass.getName())
                        .append(" ")
                        .append(actualFieldName)
                        .append(") {\n")
                        .append("this.")
                        .append(actualFieldName)
                        .append(" = ")
                        .append(actualFieldName)
                        .append(";\n")
                        .append("}");
                PsiMethod constructor = elementFactory.createMethodFromText(constructorText.toString(), assertClass);
                assertClass.add(constructor);

                StringBuilder mainAssertionText = new StringBuilder()
                        .append("public static ")
                        .append(assertClassName)
                        .append(" assert")
                        .append(psiClass.getName())
                        .append("(")
                        .append(psiClass.getName())
                        .append(" ")
                        .append(actualFieldName)
                        .append(") {\n")
                        .append("return new ")
                        .append(assertClassName)
                        .append("(")
                        .append(actualFieldName)
                        .append(");")
                        .append("}");
                PsiMethod mainAssertion = elementFactory.createMethodFromText(mainAssertionText.toString(), assertClass);
                assertClass.add(mainAssertion);

                for (PsiField field : fields) {
                    String fieldCC = Character.toUpperCase(field.getName().charAt(0)) + field.getName().substring(1);
                    String fieldType = field.getTypeElement().getText();
                    String parameterName = field.getName();
                    StringBuilder methodText = new StringBuilder()
                            .append("public ")
                            .append(assertClassName)
                            .append(" ")
                            .append("has")
                            .append(fieldCC)
                            .append("(")
                            .append(fieldType)
                            .append(" ")
                            .append(parameterName)
                            .append(") {\n")
                            .append("org.assertj.core.api.Assertions.assertThat(")
                            .append(actualFieldName)
                            .append(".get")
                            .append(fieldCC)
                            .append("()).isEqualTo(")
                            .append(parameterName)
                            .append(");\n")
                            .append("return this;\n")
                            .append("}");
                    PsiMethod assertMethod = elementFactory.createMethodFromText(methodText.toString(), assertClass);
                    assertClass.add(assertMethod);
                }

                JavaCodeStyleManager.getInstance(getProject()).shortenClassReferences(assertClass);

                VirtualFile baseDir = getProject().getBaseDir();
                VirtualFile srcTestJava = baseDir.findFileByRelativePath("src/test/java");
                PsiManager psiManager = PsiManager.getInstance(getProject());
                PsiDirectory directory = psiManager.findDirectory(srcTestJava);
                String substring = psiClass.getQualifiedName().substring(0, psiClass.getQualifiedName().length() - psiClass.getName().length());
                String[] split = substring.split("\\.");
                StringBuilder path = new StringBuilder(srcTestJava.getPath());
                for (String s : split) {
                    path.append(File.separator).append(s);
                }
                VirtualFile directories = VfsUtil.createDirectories(path.toString());
                PsiDirectory directory1 = PsiDirectoryFactory.getInstance(getProject()).createDirectory(directories);
                directory1.add(assertClass);
            }
        }.execute();
    }

    @Override
    public void update(AnActionEvent e) {
        PsiClass psiClass = getPsiClassFromEvent(e);
        e.getPresentation().setEnabled(psiClass != null);
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
