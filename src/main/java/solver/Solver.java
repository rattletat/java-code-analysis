package solver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Optional;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.visitor.VoidVisitor;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

public class Solver {

    private static final String RSC_PATH = "src/main/resources";
    private static File currentFile = null;

    private static CSVHandler fileHandler;

    public static void main(String[] args) throws Exception {
        ProjectHandler resourceRoot = new ProjectHandler(RSC_PATH);
        for (File project : resourceRoot.getSubfolders()) {
            ProjectHandler projectHandler = new ProjectHandler(project.getPath());
            for (File version : projectHandler.getSubfolders()) {
                String versionPath = version.getPath();
                fileHandler = new CSVHandler(project.getPath() + "/" + version.getName() + "-Method-results.csv");
                for (File file : ProjectHandler.getSubfolderJavaClasses(versionPath)) {
                    currentFile = file;
                    CompilationUnit cu = JavaParser.parse(new FileInputStream(file));
                    VoidVisitor<?> methodNameVisitor = new MethodNamePrinter();
                    methodNameVisitor.visit(cu, null);
                }
                fileHandler.close();
            }
        }
    }


    private static class MethodNamePrinter extends VoidVisitorAdapter<Void> {

        @Override
        public void visit(MethodDeclaration md, Void arg) {
            super.visit(md, arg);
            System.out.println("==========================================");
            System.out.println("File: " + currentFile);
            System.out.println("Method Name: " + md.getName());
            Optional<BlockStmt> blk = md.getBody();
            if (blk.isPresent()) {
                BlockStmt block = blk.get();
                String text = block.toString();
                String signature = md.getSignature().asString();
                int words = Metrics.getWordCount(text);
                int lines = Metrics.getLineCount(text);
                int repWords = Metrics.getMaxRepeatedWords(text);
                float density = Metrics.getDensity(words, lines);
                int arguments = Metrics.getParCount(md);
                int funCalls = Metrics.getFuncCallCount(md);
                int variables = Metrics.getLocalVarCount(md);
                float commentRatio = Metrics.getCommentRatio(md);
                int maxDepth = Metrics.getMaxDepth(md);
                int blocks = Metrics.getBlockCount(md);

                StaticResult result = new StaticResult(
                    currentFile,
                    signature,
                    words,
                    lines,
                    density,
                    arguments,
                    funCalls,
                    variables,
                    repWords,
                    commentRatio,
                    maxDepth,
                    blocks
                );

                System.out.println("==========================================");
                System.out.println(md);
                System.out.println("==========================================");
                System.out.println(result);
                System.out.println("==========================================");

                String[] headerRecord = result.getHeader();
                String[] lineRecord = result.getRecord();
                try {
                    fileHandler.writeCSVFile(headerRecord, lineRecord);
                } catch (IOException e) {
                    System.err.println(e);
                    e.printStackTrace();
                    System.exit(1);
                }
            } else {
                System.out.println("No method block.");
            }
        }
    }
}
