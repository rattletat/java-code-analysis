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

    public static void createMethodLineDir(File versionDir) throws Exception {
        String OUT_PATH = ProjectHandler.getOutputPath(versionDir);
        CSVHandler csvMethodHandler = new CSVHandler(OUT_PATH + "/" + "Method_Line_Dir.csv");
        List<String> lines = new LinkedList<>();
        lines.add("File,Method,From,To");

        for (File file : ProjectHandler.getSubfolderJavaClasses(versionDir)) {
            currentFile = file;
            CompilationUnit cu = JavaParser.parse(new FileInputStream(file));
            VoidVisitor<List<String>> methodNameVisitor = new MethodNamePrinter();
            System.out.println(file.getPath());
            methodNameVisitor.visit(cu, lines);
        }
        csvMethodHandler.writeData(lines);
        csvMethodHandler.close();
    }

    private static class MethodNamePrinter extends VoidVisitorAdapter<List<String>> {

        @Override
        public void visit(MethodDeclaration md, List<String> collector) {
            super.visit(md, collector);
            System.out.println(md.getSignature().asString());
            if(md.isAbstract() || !md.getBody().isPresent()) return;
            int methodBeginning = md.getBody().get().getRange().get().begin.line;
            int methodEnding = md.getBody().get().getRange().get().end.line;
            String signature = md.getSignature()
                               .asString()
                               .replaceAll(",", "|")
                               .replaceAll(" ", "");
            String line = currentFile.getPath() + "," + signature 
                + "," + methodBeginning 
                + "," + methodEnding;
            collector.add(line);
        }
    }
}
