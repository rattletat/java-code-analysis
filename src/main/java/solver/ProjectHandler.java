package solver;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;

public class ProjectHandler {
    
    protected static List<File> getSubfolderClasses(String path) {
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
        return files;
    }

}
