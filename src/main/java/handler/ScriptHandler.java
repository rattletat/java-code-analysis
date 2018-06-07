package handler;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class ScriptHandler {

    private String testMetricTool = "src/main/python/matrix_tools/metric_loc.py";
    private String coverageMetricTool = "src/main/python/matrix_tools/fault_loc.py";

  protected void runMetricTool(File matrix, File output) throws IOException, InterruptedException {
        String matrixPath = matrix.getPath();
        String outputPath = output.getPath();
        String command = "python " + testMetricTool
                         + " -m " + matrixPath
                         + " -w " + outputPath
                         + " --verbose";
        runExternalCommand(command);
    }

  protected void runCoverageTool(File matrix, File spectra, File output) throws IOException, InterruptedException {
        String matrixPath = matrix.getPath();
        String spectraPath = spectra.getPath();
        String outputPath = output.getPath();
        String command = "python " + coverageMetricTool
                         + " -m " + matrixPath
                         + " -s " + spectraPath
                         + " -w " + outputPath
                         + " --verbose";
        runExternalCommand(command);
    }

    private void runExternalCommand(String command) throws IOException, InterruptedException {
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
