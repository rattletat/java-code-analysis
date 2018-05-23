package solver;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.Validate;

public class ProjectHandler {

    private String projectPath;
    private File dir;

    public ProjectHandler(String projectPath) {
        this.projectPath = projectPath;
        this.dir = new File(projectPath);
        Validate.isTrue(dir.isDirectory(), "Project path is not a directory");
    }

    protected static List<File> getSubfolderJavaClasses(String path) {
        File rootFile = new File(path);
        List<File> files = new LinkedList<File>();
        if (rootFile.isDirectory()) {
            files = (List<File>) FileUtils.listFiles(rootFile, new String[] { "java" }, true);
        } else if (rootFile.isFile()) {
            files.add(rootFile);
        } else {
            System.err.println("Specified file is not regular.");
            System.exit(1); // TODO: Throw exception
        }
        Predicate<File> notInJavaDir = f -> !f.getPath().contains("/java/");
        files.removeIf(notInJavaDir);
        return files;
    }

    // protected int getProjectVersionsCount() {
    //     File rootFile = new File(this.projectPath);
    //     File[] dirs = rootFile.listFiles(File::isDirectory);
    //     return dirs.length;
    // }

    protected File[] getSubfolders() {
        File rootFile = new File(this.projectPath);
        File[] dirs = rootFile.listFiles(File::isDirectory);
        return dirs;
    }

    // protected File getProjectVersion(int version) {
    //     File rootFile = new File(this.projectPath);
    //     File[] dirs = rootFile.listFiles(File::isDirectory);
    //     Arrays.sort(dirs);
    //     Validate.isTrue(0 <= version && version < dirs.length, "Version index out of bounds.");
    //     return dirs[version];
    // }

    // protected String getProjectPath() {
    //     return projectPath;
    // }

}
