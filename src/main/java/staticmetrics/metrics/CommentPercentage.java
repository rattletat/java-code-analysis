package staticmetrics.metrics;

import java.util.List;
import java.util.Optional;

import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.stmt.BlockStmt;

import exceptions.MethodHasNoBodyException;

/**
 * Calculates the percentage of comments in a given method.
 * Only letters and digits are used.
 * A JavaDoc comment is considered part of the method.
 */
public class CommentPercentage extends StaticMetric {

    <T extends CallableDeclaration<T>> CommentPercentage(T md) throws MethodHasNoBodyException {
        super(md, "S-ComPer");
    }

    protected <T extends CallableDeclaration<T>> float calculate(T md) {
        BlockStmt blk = super.getBlock(md);
        String methodText = blk.toString();
        List<Comment> innerComments = md.getAllContainedComments();
        Optional<JavadocComment> docComments = md.getJavadocComment();
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
}
