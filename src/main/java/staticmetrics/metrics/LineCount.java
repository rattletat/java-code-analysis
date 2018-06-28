package staticmetrics.metrics;

import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;

import exceptions.MethodHasNoBodyException;

/**
 * Counts the lines of a method.
 */
public class LineCount extends StaticMetric {

    <T extends CallableDeclaration<T>> LineCount(T md) throws MethodHasNoBodyException {
        super(md, "S-LineCount");
    }

    protected <T extends CallableDeclaration<T>> float calculate(T md) {
        BlockStmt blk = super.getBlock(md);
        String text = blk.toString();
        String[] lines = text.split("\r\n|\r|\n");
        return lines.length - 2;
    }
}
