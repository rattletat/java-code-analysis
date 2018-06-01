package staticmetrics.metrics;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;

import staticmetrics.MethodHasNoBodyException;

/**
 * Calculates the number of the most repeated word in a method.
 */
public class RepeatedWords extends StaticMetric {

    RepeatedWords(MethodDeclaration md) throws MethodHasNoBodyException {
        super(md, "S-RepWords");
    }

    protected float calculate(MethodDeclaration md) {
        Optional<BlockStmt> blk = md.getBody();
        String text = blk.get().toString();
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
