package solver;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
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
import java.util.Set;

public class Solver {

    private static final String FILE_PATH = "src/main/resources/Time/src/main/java/";
    private static final String CSV_PATH = "results.csv";
    private static File currentFile = null;

    // Do not change
    private static final String SRC_PATH = "src/main/resources/Time/";

    public static void main(String[] args) throws Exception {
        for(File file : getSubfolderClasses(FILE_PATH)){
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
            System.out.println("File: " + currentFile);
            System.out.println("Method Name: " + md.getName());
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
                int maxDepth = getMaxDepth(md);

                System.out.println("==========================================");
                System.out.println(md);
                System.out.println("#Words: " + words);
                System.out.println("#Lines: " + lines);
                System.out.println("Density: " + density);
                System.out.println("#Arguments: " + arguments);
                System.out.println("#Method Calls: " + funcCalls);
                System.out.println("#Variables: " + variables);
                System.out.println("Maximal repeated words: " + repWords);
                System.out.println("Comments ratio: " + commentRatio);
                System.out.println("Maximal depth: " + maxDepth);
                System.out.println("");
                System.out.println("==========================================");
            }
        }
    }

    private static List<File> getSubfolderClasses(String path){
        File rootFile = new File(path);
        List<File> files = new LinkedList<File>();
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
        List<MethodCallExpr> methodCalls = Navigator.findAllNodesOfGivenClass(md, MethodCallExpr.class);
        return methodCalls.size();
    }
        // TypeSolver jpTypeSolver = new JavaParserTypeSolver(SRC_PATH);
        // TypeSolver reTypeSolver = new ReflectionTypeSolver();
        // TypeSolver combSolver = new CombinedTypeSolver(jpTypeSolver, reTypeSolver);

        // Set<String> set = new HashSet<String>();
        // for(MethodCallExpr element : methodCalls) {
            // set.add(element.toString()); 
            // System.out.println(element);
        // }


        // methodCalls.forEach(mc -> set.add(JavaParserFacade.get(combSolver)
                    // .solve(mc)
                    // .getCorrespondingDeclaration()
                    // .getQualifiedSignature()));

    private static int getLocalVarCount(MethodDeclaration md){
        List<VariableDeclarator> declarations = Navigator.findAllNodesOfGivenClass(md, VariableDeclarator.class);
        return declarations.size();
    }
    
    private static float getCommentRatio(MethodDeclaration md){
        List<Comment> innerComments = md.getAllContainedComments();
        Optional<JavadocComment> docComments = md.getJavadocComment();
        String methodText = md.getBody().toString();
        // Extract inner method comments and javadoc comments
        String commentText = innerComments.toString();
        if(docComments.isPresent()) commentText += docComments.get().toString();
        // Delete everything except letters and numerals
        String cleanedComments = commentText.replaceAll("[^\\p{L}\\p{Nd}]+", "").trim();
        String cleanedMethod = methodText.replaceAll("[^\\p{L}\\p{Nd}]+", "").trim();
        // Calculate percentage of comments
        int commentChars = cleanedComments.toCharArray().length;
        int methodChars = cleanedMethod.toCharArray().length;
        if (commentChars > 0 || methodChars > 0) {
            return (float) commentChars / (methodChars + commentChars);
        }
        return 0;
    }

    private static int getMaxDepth(MethodDeclaration md){
        List<Node> frontier = new LinkedList<Node>();
        frontier.add(md);
        int depth = 0;
        while(!frontier.isEmpty()){
            List<Node> children = new LinkedList<Node>();
            for(Node node : frontier){
                children.addAll(node.getChildNodesByType(BlockStmt.class));
            }
            frontier.clear();
            if(!children.isEmpty()){
                frontier.addAll(children);
                depth++;
            }
        }
        return depth;
    }

    private static int getBlockCount(MethodDeclaration md){
        List<BlockStmt> blocks = Navigator.findAllNodesOfGivenClass(md, BlockStmt.class);
        return blocks.size();
    }

}
