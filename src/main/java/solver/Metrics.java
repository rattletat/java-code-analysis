package solver;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.symbolsolver.javaparser.Navigator;

public class Metrics {

    /**
     * Counts the lines of String.
     * @param text Text which is counted.
     * @return Number of lines in given String.
     */
    protected static int getLineCount(String text) {
        String[] lines = text.split("\r\n|\r|\n");
        return lines.length - 2;
    }

    /**
     * Counts the words of a text.
     * @param text Text which is counted.
     * @return Number of words in given String.
     */
    protected static int getWordCount(String text) {
        String cleaned = text
                         .replaceAll("[^\\p{L}\\p{Nd}]+", " ")
                         .trim();
        String[] words = cleaned.split(" ");
        return words.length;
    }

    /**
     * Calculates the density metric of text,
     * which is 'word per line'.
     * @param words Number of words
     * @param lines Number of lines
     * @return Density: words per line
     */
    protected static float getDensity(int words, int lines) {
        if (lines == 0) return 0;
        return (float) words / lines;
    }

    /**
     * Counts the number of parameters in a given method.
     * @param method The method of which the metric is calculated.
     * @return Number of parameter in the given method.
     */
    protected static int getParCount(MethodDeclaration method) {
        NodeList<Parameter> parameters = method.getParameters();
        return parameters.size();
    }

    /**
     * Counts the amount of function calls in a method.
     * @param method The method of which the metric is calculated.
     * @return Number of function calls in the given method.
     */
    protected static int getFuncCallCount(MethodDeclaration md) {
        List<MethodCallExpr> methodCalls = Navigator
                                           .findAllNodesOfGivenClass(md, MethodCallExpr.class);
        return methodCalls.size();
    }

    /**
     * Counts the amount of local variables in a method.
     * @param method The method of which the metric is calculated.
     * @return Number of local variables in the given method.
     */
    protected static int getLocalVarCount(MethodDeclaration md) {
        List<VariableDeclarator> declarations = Navigator
                                                .findAllNodesOfGivenClass(md, VariableDeclarator.class);
        return declarations.size();
    }

    /**
     * Calculates the number of the most repeated word in a text.
     * @param method The method of which the metric is calculated.
     * @return Number of repetitions.
     */
    protected static int getMaxRepeatedWords(String text) {
        String cleaned = text
                         .replaceAll("[^\\p{L}\\p{Nd}]+", " ")
                         .trim();
        String[] words = cleaned.split(" ");
        Map<String, Integer> occurences = new HashMap<String, Integer>();
        for (String word : words) {
            Integer oldCount = occurences.get(word);
            if (oldCount == null) {
                oldCount = 0;
            }
            occurences.put(word, oldCount + 1);
        }
        return Collections.max(occurences.values());
    }

    /**
     * Calculates the percentage of comments in a given method.
     * Only letters and digits are used.
     * JavaDoc comment is considered part of the method.
     * @param method The method of which the metric is calculated.
     * @return Percentage of comments in the given method
     */
    protected static float getCommentRatio(MethodDeclaration md) {
        List<Comment> innerComments = md.getAllContainedComments();
        Optional<JavadocComment> docComments = md.getJavadocComment();
        String methodText = md.getBody().toString();
        // Extract inner method comments and javadoc comments
        String commentText = innerComments.toString();
        if (docComments.isPresent()) commentText += docComments.get().toString();
        // Delete everything except letters and numerals
        String cleanedComments = commentText
                                 .replaceAll("[^\\p{L}\\p{Nd}]+", "").trim();
        String cleanedMethod = methodText
                               .replaceAll("[^\\p{L}\\p{Nd}]+", "").trim();
        // Calculate percentage of comments
        int commentChars = cleanedComments.toCharArray().length;
        int methodChars = cleanedMethod.toCharArray().length;
        if (commentChars > 0 || methodChars > 0) {
            return (float) commentChars / (methodChars + commentChars);
        }
        return 0;
    }

    protected static int getMaxDepth(MethodDeclaration md) {
        List<Node> frontier = new LinkedList<Node>();
        frontier.add(md);
        int depth = 0;
        while (!frontier.isEmpty()) {
            List<Node> children = new LinkedList<Node>();
            for (Node node : frontier) {
                children.addAll(node.getChildNodesByType(BlockStmt.class));
            }
            frontier.clear();
            if (!children.isEmpty()) {
                frontier.addAll(children);
                depth++;
            }
        }
        return depth;
    }

    /**
     * Counts the number of Java blocks in the given method.
     * @param method The method of which the metric is calculated.
     * @return Number of blocks.
     */
    protected static int getBlockCount(MethodDeclaration md) {
        List<BlockStmt> blocks = Navigator.findAllNodesOfGivenClass(md, BlockStmt.class);
        return blocks.size();
    }
}
