package solver;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.Validate;

public class ProjectHandler {

    private File dir;

    public ProjectHandler(File project) {
        this.dir = project;
        Validate.isTrue(dir.isDirectory(), "Project path is not a directory");
    }

    public static List<File> getSubfolderJavaClasses(File rootFile) {
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
        Predicate<File> inTestDir = f -> f.getPath().contains("/test/");
        files.removeIf(notInJavaDir);
        files.removeIf(inTestDir);
        return files;
    }

    public static File[] getSubfolders(File dir) {
        File[] dirs = dir.listFiles(new FileFilter() {
            public boolean accept(File file) {
                return file.isDirectory() && !file.isHidden();
            }
        });
        return dirs;
    }

    public static File getProject(File project, int versionID) throws Exception {
        File[] versions = getSubfolders(project);
        Validate.isTrue(versions.length >= 1, "Project folder has no version subfolder.");
        if (versionID >= 1 && versionID <= versions.length) {
            Arrays.sort(versions);
            return versions[versionID - 1];
        } else {
            throw new Exception();
        }
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
