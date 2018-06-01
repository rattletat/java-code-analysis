package main;
import java.io.File;

import solver.CSVHandler;
import solver.ProjectHandler;

import staticmetrics.StaticSolver;

public class Launcher {
    private static final String RSC_PATH = "src/main/resources";

    public static void main(String[] args) throws Exception {
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
                    CSVHandler csvHandler = new CSVHandler(project.getPath() + "/" + counter + "_results.csv");
                    StaticSolver.startStaticAnalysis(version, csvHandler);
                } catch (Exception e) {
                    System.err.println("[ERROR] Static analysis failed!");
                    e.printStackTrace();
                }
                counter++;
            }
        }
    }
}
