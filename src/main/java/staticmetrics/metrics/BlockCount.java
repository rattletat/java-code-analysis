package staticmetrics.metrics;

import java.util.List;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;

import staticmetrics.MethodHasNoBodyException;

/**
 * Counts the number of Java blocks in a given method.
 */
public class BlockCount extends StaticMetric {

    BlockCount(MethodDeclaration md) throws MethodHasNoBodyException {
        super(md, "S-BlockCount");
    }

    protected float calculate(MethodDeclaration md) {
        List<BlockStmt> blocks = md.findAll(BlockStmt.class);
        return blocks.size();
    }
}
