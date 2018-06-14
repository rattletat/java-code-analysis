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

    public static void startStaticAnalysis(
        File versionDir,
        CSVHandler csvProjectHandler,
        CSVHandler csvClassHandler,
        CSVHandler csvMethodHandler,
        CSVHandler csvMethodLineHandler
    ) throws Exception {

        List<List<String>> lines = new LinkedList<>();
        for (File file : ProjectHandler.getSubfolderJavaClasses(versionDir)) {
            currentFile = file;
            CompilationUnit cu = JavaParser.parse(new FileInputStream(file));
            VoidVisitor<List<List<String>>> methodNameVisitor = new MethodNamePrinter();
            methodNameVisitor.visit(cu, lines);
        }
        List<String> header = lines.remove(0);
        System.out.println(header);
        csvMethodHandler.writeLines(header, lines);
        csvMethodHandler.close();

        // File average
        calculateFileAverage(csvMethodHandler, csvClassHandler);
        csvClassHandler.close();
        calculateProjectAverage(csvClassHandler, csvProjectHandler);
        csvProjectHandler.close();
        MethodLineSolver.createMethodLineDir(versionDir, csvMethodLineHandler);
    }

    private static class MethodNamePrinter extends VoidVisitorAdapter<List<List<String>>> {

        @Override
        public void visit(MethodDeclaration md, List<List<String>> collector) {
            super.visit(md, collector);
            // System.out.println("File: " + currentFile);
            // System.out.println("Method Name: " + md.getName());
            // System.out.println("=============================");
            // System.out.println(md);
            // System.out.println("=============================");

            StaticResult result;
            try {
                result = new StaticResult(currentFile, md);
            } catch (MethodHasNoBodyException e) {
                return;
            }
            // System.out.println(result);
            // System.out.println("=============================");
            if (collector.isEmpty()) collector.add(result.getHeader());
            collector.add(result.getValues());
        }
    }

    private static void calculateFileAverage(CSVHandler src, CSVHandler dst) throws IOException {
        List<String[]> data = src.getData();
        LinkedList<String> header = new LinkedList<>(Arrays.asList(data.remove(0)));
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
            List<String> values = new LinkedList<>();
            values.add(filePath);
            values.addAll(doubleToStringList(fileAverage));
            dst.writeLine(header, values);
        }
    }

    private static void calculateProjectAverage(CSVHandler src, CSVHandler dst) throws IOException {
        List<String[]> data = src.getData();
        List<String> header = new LinkedList<>(Arrays.asList(data.remove(0))); // remove header from data
        header.remove(0); // remove 1. column: filepath from header
        Double[] temp = new Double[data.get(0).length - 1];
        Arrays.fill(temp, 0.0);
        Double[] averages = data.stream().map(valueArray ->
                                              Arrays.stream(valueArray)
                                              .skip(1) // Skip 1. column: filepath
                                              .map(Double::parseDouble)
                                              .toArray(Double[]::new))
        .reduce(temp, (acc, elem) -> {
            Arrays.setAll(acc, (i) -> acc[i] + (elem[i] * 1.0 / data.size()));
            return acc;
        });
        List<String> projectAverage = doubleToStringList(averages);
        dst.writeLine(header, projectAverage);
    }

    private static List<String> doubleToStringList(Double[] array) {
        LinkedList<String> list = new LinkedList<>();
        for (int i = 0; i < array.length; i++) {
            list.add(String.valueOf(array[i]));
        }
        return list;
    }
}
