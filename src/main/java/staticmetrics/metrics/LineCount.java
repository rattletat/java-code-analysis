package staticmetrics.metrics;

import java.util.Optional;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;

import staticmetrics.MethodHasNoBodyException;

/**
 * Counts the lines of a method.
 */
public class LineCount extends StaticMetric {

    LineCount(MethodDeclaration md) throws MethodHasNoBodyException {
        super(md, "S-LineCount");
    }

    protected float calculate(MethodDeclaration md) {
        Optional<BlockStmt> blk = md.getBody();
        String text = blk.get().toString();
        String[] lines = text.split("\r\n|\r|\n");
        return lines.length - 2;
    }
}
