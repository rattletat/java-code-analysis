package staticmetrics.metrics;

import java.util.List;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.symbolsolver.javaparser.Navigator;

import staticmetrics.MethodHasNoBodyException;

/**
 * Counts the amount of local variables in a method.
 */
public class VariableCount extends StaticMetric {

    VariableCount(MethodDeclaration md) throws MethodHasNoBodyException {
        super(md, "S-VarCount");
    }

    protected float calculate(MethodDeclaration md) {
        List<VariableDeclarator> declarations = Navigator.findAllNodesOfGivenClass(md, VariableDeclarator.class);
        return declarations.size();
    }
}
