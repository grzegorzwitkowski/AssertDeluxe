package assertdeluxe;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiManager;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.jetbrains.jps.model.java.JavaModuleSourceRootTypes.TESTS;

public class TestSourceRootProvider {

    public List<PsiDirectory> getTestSourceRoots(Project project) {
        List<VirtualFile> roots = getTestSourceRootsFromProject(project);
        PsiManager psiManager = PsiManager.getInstance(project);
        return roots.stream().map(psiManager::findDirectory).collect(toList());
    }

    public PsiDirectory getTestSourcesRoot(Project project) {
        VirtualFile root = getTestSourceRootsFromProject(project).get(0);
        PsiManager psiManager = PsiManager.getInstance(project);
        return psiManager.findDirectory(root);
    }

    private List<VirtualFile> getTestSourceRootsFromProject(Project project) {
        ProjectRootManager projectRootManager = ProjectRootManager.getInstance(project);
        return projectRootManager.getModuleSourceRoots(TESTS);
    }
}
