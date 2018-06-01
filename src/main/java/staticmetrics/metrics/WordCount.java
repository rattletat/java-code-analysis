package staticmetrics.metrics;

import java.util.Optional;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;

import staticmetrics.MethodHasNoBodyException;

/**
 * Counts the words of a method.
 */
public class WordCount extends StaticMetric {

    WordCount(MethodDeclaration md) throws MethodHasNoBodyException {
        super(md, "S-WordCount");
    }

    protected float calculate(MethodDeclaration md) {
        Optional<BlockStmt> blk = md.getBody();
        String text = blk.get().toString();
        String cleaned = text
                         .replaceAll("[^\\p{L}\\p{Nd}]+", " ")
                         .trim();
        String[] words = cleaned.split(" ");
        return words.length;
    }
}
