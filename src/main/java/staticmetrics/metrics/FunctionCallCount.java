package staticmetrics.metrics;

import java.util.List;

import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;

import exceptions.MethodHasNoBodyException;

/**
 * Counts the amount of function calls in a method.
 */
public class FunctionCallCount extends StaticMetric {

    <T extends CallableDeclaration<T>> FunctionCallCount(T md) throws MethodHasNoBodyException {
        super(md, "S-FunCalls");
    }

    protected <T extends CallableDeclaration<T>> float calculate(T md) {
        List<MethodCallExpr> methodCalls = md.findAll(MethodCallExpr.class);
        return methodCalls.size();
    }
}
