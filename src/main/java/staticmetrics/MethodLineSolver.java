package staticmetrics;

import java.io.File;
import java.io.FileInputStream;
import java.util.LinkedList;
import java.util.List;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.body.CallableDeclaration.Signature;
import com.github.javaparser.ast.body.ConstructorDeclaration;
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
        public void visit(ConstructorDeclaration cd, List<List<String>> collector) {
            super.visit(cd, collector);
            if (cd.isAbstract() || !cd.getRange().isPresent()) return;
            String methodBeginning = getBeginningLine(cd);
            String methodEnding = getEndingLine(cd);
            String signature = cleanSignature(cd.getSignature());

            List<String> line = new LinkedList<String>() {
                {
                    add(shortenStringUntilOrg(currentFile.getPath()));
                    add(signature);
                    add(methodBeginning);
                    add(methodEnding);
                }
            };
            collector.add(line);
        }

        @Override
        public void visit(MethodDeclaration md, List<List<String>> collector) {
            super.visit(md, collector);
            if (md.isAbstract() || !md.getBody().isPresent()) return;

            String methodBeginning = getBeginningLine(md);
            String methodEnding = getEndingLine(md);
            String signature = cleanSignature(md.getSignature());

            List<String> line = new LinkedList<String>() {
                {
                    add(shortenStringUntilOrg(currentFile.getPath()));
                    add(signature);
                    add(methodBeginning);
                    add(methodEnding);
                }
            };
            collector.add(line);
        }
    }

    private static String getBeginningLine(CallableDeclaration<?> cd) {
        if (cd instanceof MethodDeclaration) {
            return String.valueOf(((MethodDeclaration) cd)
                                  .getBody().get()
                                  .getBegin().get()
                                  .line);
        }
        if (cd instanceof ConstructorDeclaration) {
            return String.valueOf(((ConstructorDeclaration) cd)
                                  .getBody()
                                  .getBegin().get()
                                  .line);
        }
        throw new RuntimeException();
    }

    private static String getEndingLine(CallableDeclaration<?> cd) {
        if (cd instanceof MethodDeclaration) {
            return String.valueOf(((MethodDeclaration) cd)
                                  .getBody().get()
                                  .getEnd().get()
                                  .line);
        }
        if (cd instanceof ConstructorDeclaration) {
            return String.valueOf(((ConstructorDeclaration) cd)
                                  .getBody()
                                  .getEnd().get()
                                  .line);
        }
        throw new RuntimeException();
    }

    private static String cleanSignature(Signature signature) {
        return signature
               .asString()
               .replaceAll(",", "|")
               .replaceAll(" ", "");
    }

    // Filter in ProjectHandler guarantees existence of substring org
    private static String shortenStringUntilOrg(String path) {
        return path.substring(path.indexOf("org"));
    }
}
