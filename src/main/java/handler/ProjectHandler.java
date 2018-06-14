package handler;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.Validate;

public class ProjectHandler {

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
        Predicate<File> notJavaInPath = f -> !f.getPath().contains("java");
        Predicate<File> notSrcPath = f -> !f.getPath().contains("/src/");
        Predicate<File> inTestDir = f -> f.getPath().contains("/test/");
        files.removeIf(notJavaInPath);
        files.removeIf(notSrcPath);
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

    public static File getProject(File project, int versionID) throws  IllegalArgumentException {
        File[] versions = getSubfolders(project);
        Validate.isTrue(versions.length >= 1, "Project folder has no version subfolder.");
        if (versionID >= 1 && versionID <= versions.length) {
            // Sort numerically, not alphanumerically
            Arrays.sort(versions, new Comparator<File>() {
                @Override
                public int compare(File o1, File o2) {
                    int n1 = Integer.parseInt(o1.getName());
                    int n2 = Integer.parseInt(o2.getName());
                    return n1 - n2;
                }
            });
            return versions[versionID - 1];
        } else {
            throw new IllegalArgumentException();
        }
    }

    // SRC: https://stackoverflow.com/questions/9884514
    // Small modifications
    public static void cloneFolder(String source, String target, int depth) {
        if (depth <= 0) return;
        File targetFile = new File(target);
        if (!targetFile.exists()) {
            targetFile.mkdir();
        }
        for (File f : new File(source).listFiles()) {
            if (f.isDirectory() && !f.isHidden()) {
                String append = "/" + f.getName();
                System.out.println("Creating '" + target + append + "': "
                                   + new File(target + append).mkdir());
                cloneFolder(source + append, target + append, depth - 1);
            }
        }
    }

    // Converts a maven project path .../resources/[project]/[version] to the equivalent result output directory.
    public static String getResultDirPath(File realVersionProject, String topLevelResultDirName) {
        String realVersionPath = realVersionProject.getPath();
        // 18 chars: src/main/resources
        return topLevelResultDirName + realVersionPath.substring(18, realVersionPath.length());
    }
}
