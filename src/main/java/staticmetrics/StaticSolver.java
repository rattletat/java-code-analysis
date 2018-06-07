package staticmetrics;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Validate;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitor;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import handler.CSVHandler;
import handler.ProjectHandler;
import staticmetrics.metrics.StaticResult;

public class StaticSolver {

    private static File currentFile = null;

    private static CSVHandler csvMethodHandler;
    // private static CSVHandler csvFileHandler;

    public static void startStaticAnalysis(File versionDir, String OUT_PATH) throws Exception {
        List<String> lines = new LinkedList<>();

        csvMethodHandler = new CSVHandler(OUT_PATH + "/" + "Static_Method_Results.csv");
        CSVHandler csvFileHandler = new CSVHandler(OUT_PATH + "/" + "Static_File_Results.csv");
        CSVHandler csvProjectHandler = new CSVHandler(OUT_PATH + "/" + "Static_Project_Results.csv");

        for (File file : ProjectHandler.getSubfolderJavaClasses(versionDir)) {
            currentFile = file;
            CompilationUnit cu = JavaParser.parse(new FileInputStream(file));
            VoidVisitor<List<String>> methodNameVisitor = new MethodNamePrinter();
            methodNameVisitor.visit(cu, lines);
        }
        csvMethodHandler.writeData(lines);
        csvMethodHandler.close();

        // File average
        calculateFileAverage(csvMethodHandler, csvFileHandler);
        csvFileHandler.close();
        calculateProjectAverage(csvFileHandler, csvProjectHandler);
        csvProjectHandler.close();

        MethodLineSolver.createMethodLineDir(versionDir, OUT_PATH);
    }

    private static class MethodNamePrinter extends VoidVisitorAdapter<List<String>> {

        @Override
        public void visit(MethodDeclaration md, List<String> collector) {
            super.visit(md, collector);
            // System.out.println("==========================================");
            System.out.println("File: " + currentFile);
            System.out.println("Method Name: " + md.getName());

            StaticResult result;
            try {
                result = new StaticResult(currentFile, md);
            } catch (MethodHasNoBodyException e) {
                return;
            }
            // LinkedList<String> header = result.getHeaderRecord();
            // LinkedList<String> values = result.getValueRecord();
            // String header = result.getHeader();
            // String values = result.getValues();

            // header.addFirst("Signature");
            // header.addFirst("Path");
            // header = "Path,Signature," + header;

            // values.addFirst(cleanSignature(md.getSignature()));
            // values.addFirst(currentFile.getPath());
            // values = cleanSignature(md.getSignature()) + currentFile.getPath() + values;

            // System.out.println("==========================================");
            // System.out.println(md);
            // System.out.println("==========================================");
            // System.out.println(result);
            // System.out.println("==========================================");
            if(collector.isEmpty()) collector.add(result.getHeader());
            collector.add(result.getValues());

            // try {
            //     csvMethodHandler.writeCSVFile(header, values);
            // } catch (IOException e) {
            //     e.printStackTrace();
            //     System.exit(1);
            // }
        }
    }

    private static void calculateFileAverage(CSVHandler src, CSVHandler dst) throws IOException {
        LinkedList<String[]> data = src.getData();
        LinkedList<String> header = new LinkedList<>(Arrays.asList(data.pop()));
        header.remove(1);
        Map<String, List<String[]>> groups = data.stream().collect(Collectors.groupingBy(line -> line[0]));
        Map<String, Double[]> averages = groups.entrySet().stream()
                                         // map entries of string, string-array list to entries of string, double-array of averages
        .map(entry -> {
            Double[] temp = new Double[data.get(0).length - 2];
            Arrays.fill(temp, 0.0);
            return new AbstractMap.SimpleEntry<>(
                entry.getKey(),
                entry.getValue().stream()
                // remove the first two columns of each row and convert to double
                .map(valueArray -> Arrays.stream(valueArray)
                     .skip(2)
                     .map(Double::parseDouble)
                     .toArray(Double[]::new)
                    )
                // calculate average of each column
            .reduce(temp, (acc, elem) -> {
                Validate.isTrue(acc.length == elem.length, "Line sizes are different");
                Arrays.setAll(acc, (i) -> acc[i] + (elem[i] * (1.0 / entry.getValue().size())));
                return acc;
            })
            );
        })
        .collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue()));

        for (String filePath : averages.keySet()) {
            Double[] fileAverage = averages.get(filePath);
            // Copying averages to String array
            LinkedList<String> strvalueList = doubleToStringList(fileAverage);
            strvalueList.addFirst(filePath);
            dst.writeCSVFile(header, strvalueList);
        }
    }

    private static void calculateProjectAverage(CSVHandler src, CSVHandler dst) throws IOException {
        LinkedList<String[]> data = src.getData();
        LinkedList<String> header = new LinkedList<>(Arrays.asList(data.pop()));
        Double[] temp = new Double[data.get(0).length - 1];
        Arrays.fill(temp, 0.0);
        Double[] averages = data.stream().map(valueArray ->
                                              Arrays.stream(valueArray)
                                              .skip(1)
                                              .map(Double::parseDouble)
                                              .toArray(Double[]::new))
        .reduce(temp, (acc, elem) -> {
            Arrays.setAll(acc, (i) -> acc[i] + (elem[i] * 1.0 / data.size()));
            return acc;
        });
        LinkedList<String> projectAverage = doubleToStringList(averages);
        header.remove(0);
        dst.writeCSVFile(header, projectAverage);
    }

    private static LinkedList<String> doubleToStringList(Double[] array) {
        LinkedList<String> list = new LinkedList<>();
        for (int i = 0; i < array.length; i++) {
            list.addLast(String.valueOf(array[i]));
        }
        return list;
    }
}
