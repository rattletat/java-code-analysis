package staticmetrics.metrics;

import java.util.List;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.symbolsolver.javaparser.Navigator;

import staticmetrics.MethodHasNoBodyException;

/**
 * Counts the amount of function calls in a method.
 */
public class FunctionCallCount extends StaticMetric {

    FunctionCallCount(MethodDeclaration md) throws MethodHasNoBodyException {
        super(md, "S-FunCalls");
    }

    protected float calculate(MethodDeclaration md) {
        List<MethodCallExpr> methodCalls = Navigator.findAllNodesOfGivenClass(md, MethodCallExpr.class);
        return methodCalls.size();
    }
}
