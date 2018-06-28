package staticmetrics.metrics;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;

import exceptions.MethodHasNoBodyException;

/**
 * Calculates the number of the most repeated word in a method.
 */
public class RepeatedWords extends StaticMetric {

    <T extends CallableDeclaration<T>> RepeatedWords(T md) throws MethodHasNoBodyException {
        super(md, "S-RepWords");
    }

    protected <T extends CallableDeclaration<T>> float calculate(T md) {
        BlockStmt blk = super.getBlock(md);
        String text = blk.toString();
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
}
