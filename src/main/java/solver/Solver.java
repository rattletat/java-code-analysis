package solver;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.visitor.VoidVisitor;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.symbolsolver.javaparser.Navigator;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Solver {

    private static final String FILE_PATH = "src/main/resources/Time/src/main/java/";

    // Do not change
    private static final String SRC_PATH = "src/main/resources/Time/";

    public static void main(String[] args) throws Exception {
        for(File file : getSubfolderClasses(FILE_PATH)){
            System.out.println("File: " + file.toString());
            CompilationUnit cu = JavaParser.parse(new FileInputStream(file));
            VoidVisitor<?> methodNameVisitor = new MethodNamePrinter();
            methodNameVisitor.visit(cu, null);
        }
        // System.out.println("HALBZEIT");
        // List<String> methodNames = new ArrayList<>();
        // VoidVisitor<List<String>> methodNameCollector = new MethodNameCollector();
        // methodNameCollector.visit(cu, methodNames);
        // methodNames.forEach(n -> System.out.println("Method Name Collected: " + n));

    }

    private static class MethodNamePrinter extends VoidVisitorAdapter<Void> {

        @Override
        public void visit(MethodDeclaration md, Void arg) {
            super.visit(md, arg);
            System.out.println("Method Name Printed: " + md.getName());
            Optional<BlockStmt> blk = md.getBody();
            if (blk.isPresent()){
                BlockStmt block = blk.get();
                String text = block.toString();
                int words = getWordCount(text);
                int lines = getLineCount(text);
                int repWords = getMaxRepeatedWords(text);
                float density = getDensity(words, lines);
                int arguments = getArgCount(md);
                int funcCalls = getFuncCallCount(md);
                int variables = getLocalVarCount(md);
                float commentRatio = getCommentRatio(md);

                System.out.println("#Words: " + words);
                System.out.println("#Lines: " + lines);
                System.out.println("Density: " + density);
                System.out.println("#Arguments: " + arguments);
                System.out.println("#Method Calls: " + funcCalls);
                System.out.println("#Variables: " + variables);
                System.out.println("Maxmal repeated words: " + repWords);
                System.out.println("Comments ratio: " + commentRatio);
            }
        }
    }

    private static List<File> getSubfolderClasses(String path){
        File rootFile = new File(path);
        List<File> files = new LinkedList();
        if(rootFile.isDirectory()){
            files = (List<File>) FileUtils.listFiles(rootFile, new String[] { "java" }, true);
        } else if(rootFile.isFile()){
            files.add(rootFile);
        }
        else {
            System.out.println("File path not correct.");
            System.exit(1);
            }
        return files;
    }

    private static int getWordCount(String text) {
        String cleaned = text.replaceAll("[^\\p{L}\\p{Nd}]+", " ").trim();
        String[] words = cleaned.split(" ");
        return words.length;
    }

    private static int getMaxRepeatedWords(String text) {
        String cleaned = text.replaceAll("[^\\p{L}\\p{Nd}]+", " ").trim();
        String[] words = cleaned.split(" ");
        Map<String, Integer> occurences = new HashMap<String, Integer>();
            for(String word : words){
                Integer oldCount = occurences.get(word);
                if(oldCount == null){
                    oldCount = 0;
                }
                occurences.put(word, oldCount+1);
            }
        // System.out.println(Collections.singletonList(occurences));
        return Collections.max(occurences.values());
        
    }

    private static int getLineCount(String text) {
        String[] lines = text.split("\r\n|\r|\n");
        return lines.length - 2;
    }

    private static float getDensity(int words, int lines) {
        return (float) words/ lines;
    }

    private static int getArgCount(MethodDeclaration md){
        NodeList<Parameter> parameters = md.getParameters();
        return parameters.size();
    }

    private static int getFuncCallCount(MethodDeclaration md){
        // TypeSolver jpTypeSolver = new JavaParserTypeSolver(SRC_PATH);
        // TypeSolver reTypeSolver = new ReflectionTypeSolver();
        // TypeSolver combSolver = new CombinedTypeSolver(jpTypeSolver, reTypeSolver);

        List<MethodCallExpr> methodCalls = Navigator.findAllNodesOfGivenClass(md, MethodCallExpr.class);
        // Set<String> set = new HashSet<String>();
        // for(MethodCallExpr element : methodCalls) {
            // set.add(element.toString()); 
            // System.out.println(element);
        // }


        // methodCalls.forEach(mc -> set.add(JavaParserFacade.get(combSolver)
                    // .solve(mc)
                    // .getCorrespondingDeclaration()
                    // .getQualifiedSignature()));
        return methodCalls.size();
    }

    private static int getLocalVarCount(MethodDeclaration md){
        TypeSolver jpTypeSolver = new JavaParserTypeSolver(SRC_PATH);
        TypeSolver reTypeSolver = new ReflectionTypeSolver();
        TypeSolver combSolver = new CombinedTypeSolver(jpTypeSolver, reTypeSolver);

        List<VariableDeclarator> declarations = Navigator.findAllNodesOfGivenClass(md, VariableDeclarator.class);
        return declarations.size();
    }
    
    private static float getCommentRatio(MethodDeclaration md){
        TypeSolver jpTypeSolver = new JavaParserTypeSolver(SRC_PATH);
        TypeSolver reTypeSolver = new ReflectionTypeSolver();
        TypeSolver combSolver = new CombinedTypeSolver(jpTypeSolver, reTypeSolver);

        List<Comment> innerComments = md.getAllContainedComments();
        Optional<JavadocComment> docComments = md.getJavadocComment();

        String commentText = innerComments.toString();
        if(docComments.isPresent()) {
            commentText += docComments.get().toString();
            System.out.println(docComments.get());}
        String methodText = md.getBody().toString();

        String cleanedComments = commentText.replaceAll("[^\\p{L}\\p{Nd}]+", "").trim();
        System.out.println(cleanedComments);
        String cleanedMethod = methodText.replaceAll("[^\\p{L}\\p{Nd}]+", "").trim();

        int commentChars = cleanedComments.toCharArray().length;
        int methodChars = cleanedMethod.toCharArray().length;
        if (commentChars > 0 || methodChars > 0) {
            return (float) commentChars / (methodChars + commentChars);
        }
        return 0;
    }

    private static int getNumBlockStmt(MethodDeclaration md){
        TypeSolver jpTypeSolver = new JavaParserTypeSolver(SRC_PATH);
        TypeSolver reTypeSolver = new ReflectionTypeSolver();
        TypeSolver combSolver = new CombinedTypeSolver(jpTypeSolver, reTypeSolver);

        List<BlockStmt> declarations = Navigator.findAllNodesOfGivenClass(md, BlockStmt.class);
        return declarations.size();
    }

    private static int getMaxDepth(MethodDeclaration md){
        TypeSolver jpTypeSolver = new JavaParserTypeSolver(SRC_PATH);
        TypeSolver reTypeSolver = new ReflectionTypeSolver();
        TypeSolver combSolver = new CombinedTypeSolver(jpTypeSolver, reTypeSolver);

        List<BlockStmt> declarations = Navigator.findAllNodesOfGivenClass(md, BlockStmt.class);
        int depth = 0;
        // for (BlockStmt block : declarations){
        // }
        return depth;
    }
    // private static class MethodNameCollector extends VoidVisitorAdapter<List<String>> {
    //
    //     @Override
    //     public void visit(MethodDeclaration md, List<String> collector) {
    //         super.visit(md, collector);
    //         collector.add(md.getNameAsString());
    //     }
    // }
    //
}
