package main;
import java.io.File;

import handler.ProjectHandler;
import staticmetrics.StaticSolver;

public class Launcher {
    static final boolean RUN_STATIC_ANALYSIS = true;
    static final boolean RUN_TEST_SUITE_ANALYSIS = false;
    static final int MIN_VERSION = 1;
    static final int MAX_VERSION = 3;

    private static final String RSC_PATH = "src/main/resources";
    private static final String RESULT_PATH = "results";

    private static final String TEST_SUITE_ANALYSIS_SCRIPT = "src/main/bin/python/matrix_tools/metric_loc.py";

    public static void main(String[] args) throws Exception {
        // Initialize result folder
        ProjectHandler.cloneFolder(RSC_PATH, RESULT_PATH, 2);

        File projectRoot = new File(RSC_PATH);
        folder:
        for (File project : ProjectHandler.getSubfolders(projectRoot)) {
            int counter = MIN_VERSION;
            while (counter <= MAX_VERSION) {
                File version;
                try {
                    version = ProjectHandler.getProject(project, counter);
                } catch (IllegalArgumentException e) {
                    continue folder;
                }

                String outputPath = ProjectHandler.getOutputPath(version, RESULT_PATH);

                // Static Analysis
                if (RUN_STATIC_ANALYSIS) {
                    try {
                        StaticSolver.startStaticAnalysis(version, outputPath);
                    } catch (Exception e) {
                        System.err.println("[ERROR] Static analysis failed!");
                        e.printStackTrace();
                        System.exit(1);
                    }
                }

                // Dynamic Analysis
                try {
                } catch (Exception e) {
                    System.err.println("[ERROR] Dynamic analysis failed!");
                    e.printStackTrace();
                }

                // Test Suite Analysis
                // Important: Generate spectra files with the generateSpectraFiles.sh bash script under Java 7 first!
                if (RUN_TEST_SUITE_ANALYSIS) {
                    try {
                        Process p = Runtime
                                    .getRuntime()
                                    .exec("python " + TEST_SUITE_ANALYSIS_SCRIPT
                                          + " -m " + outputPath + "/matrix"
                                          + " -w " + outputPath + "/Test_Suite_Results.csv"
                                          + " -v");
                        p.waitFor();
                    } catch (Exception e) {
                        System.err.println("[ERROR] Test suite analysis failed!");
                        e.printStackTrace();
                    }
                }

                counter++;
            }
        }
    }
}
