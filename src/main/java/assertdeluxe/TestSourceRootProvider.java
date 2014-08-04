package assertdeluxe;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiManager;

import java.util.LinkedList;
import java.util.List;

import static org.jetbrains.jps.model.java.JavaModuleSourceRootTypes.TESTS;

public class TestSourceRootProvider {

    public List<PsiDirectory> getTestSourceRoots(Project project) {
        PsiManager psiManager = PsiManager.getInstance(project);
        List<PsiDirectory> testSourcesRoots = new LinkedList<>();
        for (VirtualFile vf : testSourceRoots(project)) {
            testSourcesRoots.add(psiManager.findDirectory(vf));
        }
        return testSourcesRoots;
    }

    public PsiDirectory getTestSourcesRoot(Project project) {
        VirtualFile root = testSourceRoots(project).get(0);
        PsiManager psiManager = PsiManager.getInstance(project);
        return psiManager.findDirectory(root);
    }

    private List<VirtualFile> testSourceRoots(Project project) {
        ProjectRootManager projectRootManager = ProjectRootManager.getInstance(project);
        return projectRootManager.getModuleSourceRoots(TESTS);
    }
}
