package staticmetrics.metrics;

import java.util.List;

import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;

import exceptions.MethodHasNoBodyException;

/**
 * Counts the amount of local variables in a method.
 */
public class VariableCount extends StaticMetric {

    <T extends CallableDeclaration<T>> VariableCount(T md) throws MethodHasNoBodyException {
        super(md, "S-VarCount");
    }

    protected <T extends CallableDeclaration<T>> float calculate(T md) {
        List<VariableDeclarator> declarations = md.findAll(VariableDeclarator.class);
        return declarations.size();
    }
}
