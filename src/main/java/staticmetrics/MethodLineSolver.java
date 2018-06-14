package staticmetrics;

import java.io.File;
import java.io.FileInputStream;
import java.util.LinkedList;
import java.util.List;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitor;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import handler.CSVHandler;
import handler.ProjectHandler;

public class MethodLineSolver {
    private static File currentFile;

    public static void createMethodLineDir(File versionDir, CSVHandler csvMethodHandler) throws Exception {
        List<List<String>> lines = new LinkedList<>();
        List<String> header = new LinkedList<>();
        header.add("File");
        header.add("Method");
        header.add("From");
        header.add("To");

        for (File file : ProjectHandler.getSubfolderJavaClasses(versionDir)) {
            currentFile = file;
            CompilationUnit cu = JavaParser.parse(new FileInputStream(file));
            VoidVisitor<List<List<String>>> methodNameVisitor = new MethodNamePrinter();
            System.out.println(file.getPath());
            methodNameVisitor.visit(cu, lines);
        }
        csvMethodHandler.writeLines(header, lines);
        csvMethodHandler.close();
    }

    private static class MethodNamePrinter extends VoidVisitorAdapter<List<List<String>>> {

        @Override
        public void visit(MethodDeclaration md, List<List<String>> collector) {
            super.visit(md, collector);
            if(md.isAbstract() || !md.getBody().isPresent()) return;

            String methodBeginning = String.valueOf(md.getBody().get().getRange().get().begin.line);
            String methodEnding = String.valueOf(md.getBody().get().getRange().get().end.line);
            String signature = md.getSignature()
                               .asString()
                               .replaceAll(",", "|")
                               .replaceAll(" ", "");

            List<String> line = new LinkedList<>();
            line.add(currentFile.getPath());
            line.add(signature); 
            line.add(methodBeginning); 
            line.add(methodEnding);
            collector.add(line);
        }
    }
}
