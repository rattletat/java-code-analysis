package staticmetrics.metrics;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;

import staticmetrics.MethodHasNoBodyException;

/**
 * Counts the number of parameters in a given method.
 */
public class ParameterCount extends StaticMetric {

    ParameterCount(MethodDeclaration md) throws MethodHasNoBodyException {
        super(md, "S-ParaCount");
    }

    protected float calculate(MethodDeclaration md) {
        NodeList<Parameter> parameters = md.getParameters();
        return parameters.size();
    }
}
