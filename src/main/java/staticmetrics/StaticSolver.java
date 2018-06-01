package staticmetrics;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedList;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.CallableDeclaration.Signature;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitor;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import solver.CSVHandler;
import solver.ProjectHandler;

import staticmetrics.metrics.StaticResult;

public class StaticSolver {

    private static File currentFile = null;

    private static CSVHandler fileHandler;

    public static void startStaticAnalysis(File versionDir, CSVHandler csvHandler) throws Exception {
        fileHandler = csvHandler;
        for (File file : ProjectHandler.getSubfolderJavaClasses(versionDir)) {
            currentFile = file;
            CompilationUnit cu = JavaParser.parse(new FileInputStream(file));
            VoidVisitor<?> methodNameVisitor = new MethodNamePrinter();
            methodNameVisitor.visit(cu, null);
        }
    }

    private static class MethodNamePrinter extends VoidVisitorAdapter<Void> {

        @Override
        public void visit(MethodDeclaration md, Void arg) {
            super.visit(md, arg);
            System.out.println("==========================================");
            System.out.println("File: " + currentFile);
            System.out.println("Method Name: " + md.getName());

            StaticResult result;
            try {
                result = new StaticResult(md);
            } catch (MethodHasNoBodyException e) {
                return;
            }
            LinkedList<String> header = result.getHeaderRecord();
            LinkedList<String> values = result.getValueRecord();

            header.addFirst("Signature");
            header.addFirst("Path");

            values.addFirst(cleanSignature(md.getSignature()));
            values.addFirst(currentFile.getPath());

            System.out.println("==========================================");
            System.out.println(md);
            System.out.println("==========================================");
            System.out.println(result);
            System.out.println("==========================================");

            try {
                fileHandler.writeCSVFile(header, values);
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
        private String cleanSignature(Signature signature) {
            String result = signature.asString();
            return result.replaceAll(",", "|").replaceAll(" ", "");
        }
    }
}
