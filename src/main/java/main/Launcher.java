package main;
import java.io.File;

import solver.ProjectHandler;

import staticmetrics.StaticSolver;

public class Launcher {
    private static final String RSC_PATH = "src/main/resources";
    private static final String TEMP_PATH = "temp";

    public static void main(String[] args) throws Exception {
        // Initialize temp folder
        ProjectHandler.cloneFolder(RSC_PATH, TEMP_PATH, 2);

        File projectRoot = new File(RSC_PATH);
        folder:
        for (File project : ProjectHandler.getSubfolders(projectRoot)) {
            int counter = 1;
            while (true) {
                File version;
                try {
                    version = ProjectHandler.getProject(project, counter);
                } catch (Exception e) {
                    continue folder;
                }

                // Static Analysis
                try {
                    System.out.println(ProjectHandler.getResourcePath(project));
                    StaticSolver.startStaticAnalysis(version);
                } catch (Exception e) {
                    System.err.println("[ERROR] Static analysis failed!");
                    e.printStackTrace();
                    System.exit(1);
                }
                // Dynamic Analysis
                try {
                } catch (Exception e) {
                    System.err.println("[ERROR] Dynamic analysis failed!");
                    e.printStackTrace();
                }

                counter++;
            }
        }
    }
}
