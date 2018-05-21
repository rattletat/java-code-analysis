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

    private static final String FILE_PATH = "src/main/resources/Time/src/main/java/";
    private static final String CSV_PATH = "results.csv";
    private static File currentFile = null;

    // Do not change
    private static final String SRC_PATH = "src/main/resources/Time/";
    private static CSVHandler fileHandler;

    public static void main(String[] args) throws Exception {
        fileHandler = new CSVHandler(CSV_PATH);

        for (File file : ProjectHandler.getSubfolderClasses(FILE_PATH)) {
            currentFile = file;
            CompilationUnit cu = JavaParser.parse(new FileInputStream(file));
            VoidVisitor<?> methodNameVisitor = new MethodNamePrinter();
            methodNameVisitor.visit(cu, null);
        }
        fileHandler.close();
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

