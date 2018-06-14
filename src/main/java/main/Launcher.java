package main;

import java.io.File;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Validate;

import handler.CSVHandler;
import handler.ProjectHandler;

import staticmetrics.StaticSolver;

public class Launcher {
    private static final boolean RUN_STATIC_ANALYSIS = false;
    private static final boolean RUN_DYNAMIC_ANALYSIS = false;
    private static final boolean RUN_TEST_SUITE_ANALYSIS = false;
    private static final boolean COMBINE_VERSION_ANALYSIS = true;
    private static final int MIN_VERSION = 1;
    private static final int MAX_VERSION = 3;

    private static final String RSC_PATH = "src/main/resources";
    private static final String RESULT_PATH = "results";

    private static final String RESULTS_FILENAME = "Overall_Results.csv";
    private static final String PROJECT_RESULTS_FILENAME = "Overall_Project_Results.csv";
    private static final String COMBINED_RESULTS_FILENAME = "Overall_Version_Results.csv";
    private static final String STATIC_PROJECT_OUTPUT_FILENAME = "Static_Project_Results.csv";
    private static final String STATIC_CLASS_OUTPUT_FILENAME = "Static_Class_Results.csv";
    private static final String STATIC_METHOD_OUTPUT_FILENAME = "Static_Method_Results.csv";
    private static final String STATIC_METHODLINE_OUTPUT_FILENAME = "Static_MethodLine_Results.csv";
    private static final String DYNAMIC_OUTPUT_FILENAME = "Dynamic_Results.csv";
    private static final String TESTSUITE_OUTPUT_FILENAME = "Test_Suite_Results.csv";

    private static final String TEST_SUITE_ANALYSIS_SCRIPT = "src/main/tools/python/matrix_tools/metric_loc.py";

    public static void main(String[] args) throws Exception {
        // Initialize result folder
        ProjectHandler.cloneFolder(RSC_PATH, RESULT_PATH, 2);
        File projectRoot = new File(RSC_PATH);
        List<Map.Entry<SimpleEntry<String, Integer>, CSVHandler>> projectResults = new LinkedList<>();

        folder:
        for (File project : ProjectHandler.getSubfolders(projectRoot)) {
            int versionNumber = MIN_VERSION;
            List<Map.Entry<Integer, CSVHandler>> versionResults = new LinkedList<>();
            String outputProjectDir = ProjectHandler.getResultDirPath(project, RESULT_PATH);
            while (versionNumber <= MAX_VERSION) {
                File version;
                try {
                    version = ProjectHandler.getProject(project, versionNumber);
                } catch (IllegalArgumentException e) {
                    continue folder;
                }
                String outputVersionDir = ProjectHandler.getResultDirPath(version, RESULT_PATH);

                // Static Analysis
                final CSVHandler staticProjectResult = new CSVHandler(
                    outputVersionDir + "/" + STATIC_PROJECT_OUTPUT_FILENAME,
                    RUN_STATIC_ANALYSIS
                );
                final CSVHandler staticClassResult = new CSVHandler(
                    outputVersionDir + "/" + STATIC_CLASS_OUTPUT_FILENAME,
                    RUN_STATIC_ANALYSIS
                );
                final CSVHandler staticMethodResult = new CSVHandler(
                    outputVersionDir + "/" + STATIC_METHOD_OUTPUT_FILENAME,
                    RUN_STATIC_ANALYSIS
                );
                final CSVHandler staticMethodLineResult = new CSVHandler(
                    outputVersionDir + "/" + STATIC_METHODLINE_OUTPUT_FILENAME,
                    RUN_STATIC_ANALYSIS
                );

                if (RUN_STATIC_ANALYSIS) {
                    try {
                        StaticSolver.startStaticAnalysis(
                            version,
                            staticProjectResult,
                            staticClassResult,
                            staticMethodResult,
                            staticMethodLineResult
                        );
                    } catch (Exception e) {
                        System.err.println("[ERROR] Static analysis failed!");
                        e.printStackTrace();
                        System.exit(1);
                    }
                }

                // Dynamic Analysis
                // String dynamicResultPath = outputVersionDir + "/" + DYNAMIC_OUTPUT_FILENAME;
                // final CSVHandler dynamicResult = new CSVHandler(dynamicResultPath, RUN_DYNAMIC_ANALYSIS);
                if (RUN_DYNAMIC_ANALYSIS) {
                    try {
                    } catch (Exception e) {
                        System.err.println("[ERROR] Dynamic analysis failed!");
                        e.printStackTrace();
                    }
                }

                // Test Suite Analysis
                // Important: Generate spectra files with the generateSpectraFiles.sh bash script under Java 7 first!
                String testSuiteResultPath = outputVersionDir + "/" + TESTSUITE_OUTPUT_FILENAME;
                final CSVHandler testSuiteResult = new CSVHandler(testSuiteResultPath, RUN_TEST_SUITE_ANALYSIS);
                if (RUN_TEST_SUITE_ANALYSIS) {
                    try {
                        Process p = Runtime
                                    .getRuntime()
                                    .exec("python " + TEST_SUITE_ANALYSIS_SCRIPT
                                          + " -m " + outputVersionDir + "/matrix"
                                          + " -w " + testSuiteResultPath
                                          + " -v");
                        p.waitFor();
                    } catch (Exception e) {
                        System.err.println("[ERROR] Test suite analysis failed!");
                        e.printStackTrace();
                    }
                }

                // Add combined result to List of version results
                String combinedResultsPath = outputVersionDir + "/" + COMBINED_RESULTS_FILENAME;
                final CSVHandler combinedResult = new CSVHandler(combinedResultsPath, COMBINE_VERSION_ANALYSIS);
                versionResults.add(new AbstractMap.SimpleEntry<Integer, CSVHandler>(versionNumber, combinedResult));
                if (COMBINE_VERSION_ANALYSIS) {
                    List<CSVHandler> csvFiles = new LinkedList<>();
                    csvFiles.add(staticProjectResult);
                    // csvFiles.add(dynamicResult);
                    csvFiles.add(testSuiteResult);
                    combinedResult.combineCSVFiles(csvFiles);
                }

                versionNumber++;
            }

            // Stack project results
            String overallProjectResultPath = outputProjectDir + "/" + PROJECT_RESULTS_FILENAME;
            final CSVHandler overallProjectResult = new CSVHandler(overallProjectResultPath, COMBINE_VERSION_ANALYSIS);
            int versionCount = versionResults.size();
            String projectName = project.getName();
            projectResults.add(new SimpleEntry<SimpleEntry<String, Integer>, CSVHandler>(
                                   new SimpleEntry<String, Integer>(projectName, versionCount),
                                   overallProjectResult
                               ));
            if (COMBINE_VERSION_ANALYSIS) {
                List<CSVHandler> versionCSVFiles = versionResults.stream()
                                                   .map(entry -> entry.getValue())
                                                   .collect(Collectors.toList());
                List<String> versionNumbers = versionResults.stream()
                                              .map(entry -> String.valueOf(entry.getKey()))
                                              .collect(Collectors.toList());
                overallProjectResult.stackCSVFiles(versionCSVFiles);
                overallProjectResult.appendLeft("Version", versionNumbers);


                // List<String> versionNumbers = versionResults.stream().
            }
        }

        final CSVHandler overallResult = new CSVHandler(RESULT_PATH + "/" + RESULTS_FILENAME, COMBINE_VERSION_ANALYSIS);
        // Stack projects
        if (COMBINE_VERSION_ANALYSIS) {
            List<CSVHandler> projectCSVFiles = projectResults.stream()
                                               .map(entry -> entry.getValue())
                                               .collect(Collectors.toList());
            List<String> projectNames = projectResults.stream()
                                        .map(entry -> String.valueOf(entry.getKey().getKey()))
                                        .collect(Collectors.toList());
            List<Integer> projectCount = projectResults.stream()
                                         .map(entry -> entry.getKey().getValue())
                                         .collect(Collectors.toList());
            overallResult.stackCSVFiles(projectCSVFiles);
            List<String> projectColumn = new LinkedList<>();
            Validate.isTrue(projectNames.size() == projectCount.size(), "Project name list size unequal to project count list size!");
            for (Integer i : projectCount) {
                String projectName = projectNames.remove(0);
                for (int j = 0; j < i; j++) {
                    projectColumn.add(projectName);
                }
            }
            overallResult.appendLeft("Project", projectColumn);
        }
    }
}
