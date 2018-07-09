package main;

import java.io.File;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Validate;

import exceptions.DotfileDoesNotExist;
import exceptions.PatchNotInMethodException;

import handler.CSVHandler;
import handler.LabelSolver;
import handler.ProjectHandler;
import handler.ScriptHandler;

import staticmetrics.StaticSolver;

import util.ProjectVersion;

public class Launcher {
    private static final boolean RUN_STATIC_ANALYSIS = true;
    private static final boolean RUN_SUSPICIOUSNESS_ANALYSIS = true;
    private static final boolean RUN_LABEL_ANALYSIS = true;
    private static final boolean RUN_DYNAMIC_ANALYSIS = true;
    private static final boolean RUN_TEST_SUITE_ANALYSIS = true;
    private static final boolean COMBINE_VERSION_ANALYSIS = true;
    private static final int MIN_VERSION = 1;
    private static final int MAX_VERSION = 200;

    // Versions without correct dotfiles, skipping these
    @SuppressWarnings("serial")
    private final static HashSet<ProjectVersion> invalidVersions = new HashSet<ProjectVersion>() {
        {
            add(new ProjectVersion("Math", 3));
            add(new ProjectVersion("Math", 6));
            add(new ProjectVersion("Math", 15));
            add(new ProjectVersion("Math", 16));
            add(new ProjectVersion("Math", 18));
            add(new ProjectVersion("Math", 19));
            add(new ProjectVersion("Math", 20));
            add(new ProjectVersion("Math", 59));
            add(new ProjectVersion("Math", 63));
            add(new ProjectVersion("Math", 79));
            add(new ProjectVersion("Chart", 4));
        }
    };

    private static final String SUSPICIOUSNESS_TECHNIQUE = "dstar2";

    private static final String RSC_PATH = "src/main/resources";
    private static final String RESULT_PATH = "results";

    private static final String RESULTS_FILENAME = "Overall_Results.csv";
    private static final String PROJECT_RESULTS_FILENAME = "Overall_Project_Results.csv";
    private static final String COMBINED_RESULTS_FILENAME = "Overall_Version_Results.csv";
    private static final String STATIC_PROJECT_OUTPUT_FILENAME = "Static_Project_Results.csv";
    private static final String STATIC_CLASS_OUTPUT_FILENAME = "Static_Class_Results.csv";
    private static final String STATIC_METHOD_OUTPUT_FILENAME = "Static_Method_Results.csv";
    private static final String STATIC_METHODLINE_OUTPUT_FILENAME = "Static_MethodLine_Results.csv";
    private static final String SUSPICIOUSNESS_OUTPUT_FILENAME = "Suspiciousness_Values.csv";
    private static final String DYNAMIC_OUTPUT_FILENAME = "Dynamic_Results.csv";
    private static final String TESTSUITE_OUTPUT_FILENAME = "Test_Suite_Results.csv";

    private static final String JSON_BUG_CSV = "src/main/json/defects4j-dissection/defects4j-bugs.json";

    public static void main(String[] args) throws Exception {
        // Initialize result folder
        ProjectHandler.cloneFolder(RSC_PATH, RESULT_PATH, 2);

        File projectRoot = new File(RSC_PATH);
        List<Map.Entry<SimpleEntry<String, Integer>, CSVHandler>> projectResults = new LinkedList<>();
        List<String> projectRanks = new LinkedList<>();
        List<String> projectFaultLocalizable = new LinkedList<>();
        List<ProjectVersion> dotFileDoesNotExist = new LinkedList<>();

        for (File project : ProjectHandler.getSubfolders(projectRoot)) {
            int versionNumber = MIN_VERSION;
            String projectName = project.getName();
            List<Map.Entry<Integer, CSVHandler>> versionResults = new LinkedList<>();
            String outputProjectDir = ProjectHandler.getResultDirPath(project, RESULT_PATH);
            while (versionNumber <= MAX_VERSION) {
                System.out.println("[STATUS] Start analysis for project " + projectName + "-" + versionNumber);
                // Check whether invalid version
                ProjectVersion pv = new ProjectVersion(projectName, versionNumber);
                if (invalidVersions.contains(pv)) {
                    System.out.println("[WARNING] Invalid version!");
                    versionNumber++;
                    continue;
                }

                File version;
                try {
                    version = ProjectHandler.getProject(project, versionNumber);
                } catch (IllegalArgumentException e) {
                    System.out.println("[WARNING] Version Number too high: " + versionNumber);
                    versionNumber++;
                    continue;
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

                // Suspiciousness Analysis
                String suspiciousnessResultPath = outputVersionDir + "/" + SUSPICIOUSNESS_OUTPUT_FILENAME;
                // Just for recreating the file
                final CSVHandler suspiciousnessResult = new CSVHandler(suspiciousnessResultPath, RUN_SUSPICIOUSNESS_ANALYSIS);
                suspiciousnessResult.close();
                if (RUN_SUSPICIOUSNESS_ANALYSIS) {
                    System.out.println("[STATUS] Start suspiciousness analysis.");
                    try {
                        ScriptHandler.runSuspiciousnessTool(
                            outputVersionDir + "/matrix",
                            outputVersionDir + "/spectra",
                            SUSPICIOUSNESS_TECHNIQUE,
                            suspiciousnessResultPath
                        );
                    } catch (Exception e) {
                        System.err.println("[ERROR] Test suite analysis failed!");
                        e.printStackTrace();
                        System.exit(1);
                    }
                    System.out.println("[STATUS] Suspiciousness analysis successful!.");
                }

                // Label analysis
                LabelSolver lSolver = new LabelSolver(JSON_BUG_CSV);
                int minimalRank = -1;
                boolean localizable = false;
                Collection<String[]> faultyMethods = new LinkedList<>();
                if (RUN_LABEL_ANALYSIS) {
                    System.out.println("[STATUS] Start label analysis.");
                    try {
                        Collection<String[]> patched_methods = lSolver.getPatchedMethods(projectName, versionNumber, staticMethodLineResult);
                        Collection<String[]> suspicious_methods = suspiciousnessResult.getData();
                        faultyMethods = lSolver.getFaultyMethods(patched_methods, suspicious_methods);
                        minimalRank = lSolver.getMinimalRankLabel(faultyMethods);
                        localizable = true;
                        System.out.println("[STATUS] Label analysis successful!.");
                    } catch (PatchNotInMethodException e) {
                        System.out.println("[WARNING] Patch not in method!");
                    }
                }

                // Dynamic Analysis
                String dynamicResultPath = outputVersionDir + "/" + DYNAMIC_OUTPUT_FILENAME;
                final CSVHandler dynamicResult = new CSVHandler(dynamicResultPath, RUN_DYNAMIC_ANALYSIS && RUN_LABEL_ANALYSIS);
                if (RUN_DYNAMIC_ANALYSIS && RUN_LABEL_ANALYSIS) {
                    System.out.println("[STATUS] Start dynamic analysis.");
                    try {
                        String dotFile = ProjectHandler.getDotFile(projectName, versionNumber);
                        String list = faultyMethods.stream()
                                      .map(array -> array[0] + "#" + array[1])
                                      .collect(Collectors.joining(","));
                        ScriptHandler.runDynamicTool(
                            dotFile,
                            list,
                            dynamicResultPath
                        );
                    } catch (DotfileDoesNotExist e) {
                        System.out.println("[WARNING] Dotfile does not exist!");
                        dotFileDoesNotExist.add(new ProjectVersion(projectName, versionNumber));
                        versionNumber++;
                        continue;
                    } catch (Exception e) {
                        System.err.println("[ERROR] Dynamic analysis failed!");
                        e.printStackTrace();
                        System.exit(1);
                    }
                    System.out.println("[STATUS] Dynamic analysis successful!.");
                }
                // Add minimal rank after dotfile exists
                projectRanks.add(String.valueOf(minimalRank));

                if(localizable)
                    projectFaultLocalizable.add(String.valueOf(true));
                else
                    projectFaultLocalizable.add(String.valueOf(false));

                // Test Suite Analysis
                // Important: Generate spectra files with the generateSpectraFiles.sh bash script under Java 7 first!
                String testSuiteResultPath = outputVersionDir + "/" + TESTSUITE_OUTPUT_FILENAME;
                final CSVHandler testSuiteResult = new CSVHandler(testSuiteResultPath, RUN_TEST_SUITE_ANALYSIS);
                if (RUN_TEST_SUITE_ANALYSIS) {
                    System.out.println("[STATUS] Start test suite analysis.");
                    try {
                        ScriptHandler.runMetricTool(
                            outputVersionDir + "/matrix",
                            testSuiteResultPath
                        );
                    } catch (Exception e) {
                        System.err.println("[ERROR] Test suite analysis failed!");
                        e.printStackTrace();
                    }
                    System.out.println("[STATUS] Test suite analysis successful!.");
                }

                // Add combined result to List of version results
                String combinedResultsPath = outputVersionDir + "/" + COMBINED_RESULTS_FILENAME;
                final CSVHandler combinedResult = new CSVHandler(combinedResultsPath, COMBINE_VERSION_ANALYSIS);
                versionResults.add(new AbstractMap.SimpleEntry<Integer, CSVHandler>(versionNumber, combinedResult));
                if (COMBINE_VERSION_ANALYSIS) {
                    System.out.println("[STATUS] Start combining versions.");
                    List<CSVHandler> csvFiles = new LinkedList<>();
                    csvFiles.add(staticProjectResult);
                    csvFiles.add(dynamicResult);
                    csvFiles.add(testSuiteResult);
                    combinedResult.combineCSVFiles(csvFiles);
                    combinedResult.close();
                    System.out.println("[STATUS] Combining versions successful!");
                }
                System.out.println("[Status] End analysis for project " + projectName + "-" + versionNumber);
                versionNumber++;
                System.gc();
            }
            if (versionNumber == MIN_VERSION) continue;

            final CSVHandler overallResult = new CSVHandler(RESULT_PATH + "/" + RESULTS_FILENAME, COMBINE_VERSION_ANALYSIS);
            // Stack project results
            String overallProjectResultPath = outputProjectDir + "/" + PROJECT_RESULTS_FILENAME;
            final CSVHandler overallProjectResult = new CSVHandler(overallProjectResultPath, COMBINE_VERSION_ANALYSIS);
            int versionCount = versionResults.size();
            projectResults.add(new SimpleEntry<SimpleEntry<String, Integer>, CSVHandler>(
                                   new SimpleEntry<String, Integer>(projectName, versionCount),
                                   overallProjectResult
                               ));
            if (COMBINE_VERSION_ANALYSIS) {
                System.out.println("[STATUS] Start stacking project results.");
                List<CSVHandler> versionCSVFiles = versionResults.stream()
                                                   .map(entry -> entry.getValue())
                                                   .collect(Collectors.toList());
                List<String> versionNumbers = versionResults.stream()
                                              .map(entry -> String.valueOf(entry.getKey()))
                                              .collect(Collectors.toList());
                overallProjectResult.stackCSVFiles(versionCSVFiles);
                overallProjectResult.appendRight("RankDistance", projectRanks);
                overallProjectResult.appendRight("FaultLocalizable", projectFaultLocalizable);
                overallProjectResult.appendLeft("Version", versionNumbers);
                overallProjectResult.close();
                System.out.println("[STATUS] Stacking project results successful!.");
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
                    String projectNameTEMP = projectNames.remove(0);
                    for (int j = 0; j < i; j++) {
                        projectColumn.add(projectNameTEMP);
                    }
                }
                overallResult.appendLeft("Project", projectColumn);
                overallResult.close();
            }
        }
        if (!dotFileDoesNotExist.isEmpty()) {
            System.out.println("Invalid versions!");
            for (ProjectVersion pv : dotFileDoesNotExist) {
                System.out.println("Dotfile missing: " + pv.projectName + " : " + String.valueOf(pv.version));
            }
        }
    }
}
