package handler;

import java.io.File;
import java.io.FileFilter;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.Validate;

import exceptions.DotfileDoesNotExist;

public class ProjectHandler {

    private static String dotfiles = "src/main/dotfiles";

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
        Predicate<File> notSrcPath = f -> !f.getPath().contains("/src/") && !f.getPath().contains("/source/");
        Predicate<File> inTestDir = f -> f.getPath().contains("/test/");
        Predicate<File> inTargetDir = f -> f.getPath().contains("/target/");
        // Predicate<File> abstractInName = f -> f.getPath().contains("Abstract")
        // || f.getPath().contains("abstract");
        // Predicate<File> inResourcesDir = f -> f.getPath().contains("/resources/");
        Predicate<File> notInOrgCom = f -> !f.getPath().contains("/org/") && !f.getPath().contains("/com/");
        files.removeIf(notSrcPath);
        files.removeIf(inTestDir);
        files.removeIf(inTargetDir);
        files.removeIf(notInOrgCom);
        // files.removeIf(abstractInName);
        // files.removeIf(inResourcesDir);

        Validate.isTrue(files.size() >= 1, "No filtered java files found in project: " + rootFile.getName());
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
        File[] files = getSubfolders(project);
        List<Integer> versions = Arrays.asList(files).stream()
                                 .map(file -> Integer.parseInt(file.getName()))
                                 .collect(Collectors.toList());
        int fileIndex = versions.indexOf(versionID);
        Validate.isTrue(files.length >= 1, "Project folder has no version subfolder.");
        if (fileIndex == -1) {
            throw new IllegalArgumentException();
        }
        return files[fileIndex];
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
                // System.out.println("Creating '" + target + append + "': "
                // + new File(target + append).mkdir());
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

    // Returns path to corresponding dotfile
  public static String getDotFile(String projectName, int versionNumber)
      throws DotfileDoesNotExist {
        String path = Paths.get(dotfiles, projectName, String.valueOf(versionNumber) + ".dot")
               .toString();
        File dotfile = new File(path);
        if(dotfile.exists() && !dotfile.isDirectory()){
         return path;
        }
        throw new DotfileDoesNotExist();
    }
}
