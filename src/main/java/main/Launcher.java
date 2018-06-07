package main;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import handler.ProjectHandler;

public class Launcher {
    private static final String RSC_PATH = "src/main/resources";
    private static final String TEMP_PATH = "temp";

    private static final String TEST_SCRIPT = "src/main/bin/python/matrix_tools/metric_loc.py";

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

                String outputPath = ProjectHandler.getOutputPath(version);

                // Static Analysis
                try {
                    // StaticSolver.startStaticAnalysis(version);
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

                // Test Suite Analysis
                // Important: Generate spectra files with the generateSpectraFiles.sh bash script under Java 7 first!
                try {
                    Process p = Runtime
                                .getRuntime()
                                .exec("python " + TEST_SCRIPT + " -m " + outputPath + "/matrix -w " + outputPath + "/Test_Suite_Results.csv -v");
                    p.waitFor();
                } catch (Exception e) {
                    System.err.println("[ERROR] Test suite analysis failed!");
                    e.printStackTrace();
                }

                counter++;
            }
        }
    }
}
