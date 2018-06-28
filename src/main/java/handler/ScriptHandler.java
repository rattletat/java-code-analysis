package handler;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class ScriptHandler {

    private static String testMetricTool = "src/main/tools/python/matrix_tools/metric_loc.py";
    private static String suspiciousnessTool = "src/main/tools/python/matrix_tools/fault_loc.py";
    private static String dynamicTool = "src/main/tools/python/dynamic_tool/dynamic_solver.py";

    public static void runMetricTool(
        String matrixPath,
        String outputPath
    ) throws IOException, InterruptedException {
        String command = "python " + testMetricTool
                         + " -m " + matrixPath
                         + " -w " + outputPath;
                         // + " --verbose";
        runExternalCommand(command);
    }

    public static void runSuspiciousnessTool(
        String matrixPath,
        String spectraPath,
        String technique,
        String outputPath
    ) throws IOException, InterruptedException {
        String command = "python " + suspiciousnessTool
                         + " -m " + matrixPath
                         + " -s " + spectraPath
                         + " -t " + technique
                         + " -w " + outputPath;
                         // + " --verbose";
        runExternalCommand(command);
    }

    public static void runDynamicTool(
        String dotFilePath,
        String faultyMethods,
        String outputPath
    ) throws IOException, InterruptedException {
        String faultyOption = "";
        if (faultyMethods.trim().length() > 0){
            faultyOption = " -f " + faultyMethods;
        }
        String command = "python " + dynamicTool
                         + " -d " + dotFilePath
                         + faultyOption
                         + " -w " + outputPath
                         + " --verbose";
        runExternalCommand(command);
    }

    private static void runExternalCommand(String command) throws IOException, InterruptedException {
        System.out.println("Calling: " + command);
        Process p = Runtime.getRuntime().exec(command);
        BufferedReader bri = new BufferedReader
        (new InputStreamReader(p.getInputStream()));
        BufferedReader bre = new BufferedReader
        (new InputStreamReader(p.getErrorStream()));
        String line;
        while ((line = bri.readLine()) != null) {
            System.out.println(line);
        }
        bri.close();
        while ((line = bre.readLine()) != null) {
            System.out.println(line);
        }
        bre.close();
        p.waitFor();
        System.out.println("Finished calling external program!");
    }
}
