package staticmetrics.metrics;

import java.util.List;
import java.util.Optional;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.comments.JavadocComment;

import staticmetrics.MethodHasNoBodyException;

/**
 * Calculates the percentage of comments in a given method.
 * Only letters and digits are used.
 * A JavaDoc comment is considered part of the method.
 */
public class CommentPercentage extends StaticMetric {

    CommentPercentage(MethodDeclaration md) throws MethodHasNoBodyException {
        super(md, "S-ComPer");
    }

    protected float calculate(MethodDeclaration md) {
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
}
