package solver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.FileUtils;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.visitor.VoidVisitor;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.opencsv.CSVWriter;

public class Solver {

    private static final String FILE_PATH = "src/main/resources/Time/src/main/java/";
    private static final String CSV_PATH = "results.csv";
    private static File currentFile = null;

    // Do not change
    private static final String SRC_PATH = "src/main/resources/Time/";

    public static void main(String[] args) throws Exception {
        for (File file : getSubfolderClasses(FILE_PATH)) {
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

                String[] headerRecord = result.getHeader();
                String[] lineRecord = result.getRecord();
                writeCSVFile(CSV_PATH, headerRecord, lineRecord);
            } else {
                System.out.println("No method block.");
            }
        }
    }

    private static List<File> getSubfolderClasses(String path) {
        File rootFile = new File(path);
        List<File> files = new LinkedList<File>();
        if (rootFile.isDirectory()) {
            files = (List<File>) FileUtils.listFiles(rootFile, new String[] { "java" }, true);
        } else if (rootFile.isFile()) {
            files.add(rootFile);
        } else {
            System.out.println("File path not correct.");
            System.exit(1);
        }
        return files;
    }

    private static void writeCSVFile(String path, String[] header, String[] args) {
        File file = new File(path);
        if (!file.exists()) {
            try (
                    Writer writer = Files.newBufferedWriter(Paths.get(path));
                    CSVWriter csvWriter = new CSVWriter(writer,
                                                        CSVWriter.DEFAULT_SEPARATOR,
                                                        CSVWriter.NO_QUOTE_CHARACTER,
                                                        CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                                                        CSVWriter.DEFAULT_LINE_END); ) {
                csvWriter.writeNext(header);
                csvWriter.writeNext(args);
            } catch (IOException e) {
                System.err.println("Error during CSV file generation! Aborting ...");
            }
        }
        else{
// TODO appending data:  <20-05-18, yourname> //
        }
    }
}
