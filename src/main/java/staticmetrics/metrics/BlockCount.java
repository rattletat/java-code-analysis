package staticmetrics.metrics;

import java.util.List;

import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;

import exceptions.MethodHasNoBodyException;

/**
 * Counts the number of Java blocks in a given method.
 */
public class BlockCount extends StaticMetric {

    <T extends CallableDeclaration<T>> BlockCount(T md) throws MethodHasNoBodyException {
        super(md, "S-BlockCount");
    }

    protected <T extends CallableDeclaration<T>> float calculate(T md) {
        List<BlockStmt> blocks = md.findAll(BlockStmt.class);
        return blocks.size();
    }
}
