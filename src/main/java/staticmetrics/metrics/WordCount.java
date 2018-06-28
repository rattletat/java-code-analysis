package staticmetrics.metrics;

import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;

import exceptions.MethodHasNoBodyException;

/**
 * Counts the words of a method.
 */
public class WordCount extends StaticMetric {

    <T extends CallableDeclaration<T>> WordCount(T md) throws MethodHasNoBodyException {
        super(md, "S-WordCount");
    }

    protected <T extends CallableDeclaration<T>> float calculate(T md) {
        BlockStmt block = super.getBlock(md);
        String text = block.toString();
        String cleaned = text
                         .replaceAll("[^\\p{L}\\p{Nd}]+", " ")
                         .trim();
        String[] words = cleaned.split(" ");
        return words.length;
    }
}
