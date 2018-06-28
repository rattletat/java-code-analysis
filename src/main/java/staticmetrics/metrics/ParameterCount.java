package staticmetrics.metrics;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.body.Parameter;

import exceptions.MethodHasNoBodyException;

/**
 * Counts the number of parameters in a given method.
 */
public class ParameterCount extends StaticMetric {

    <T extends CallableDeclaration<T>> ParameterCount(T md) throws MethodHasNoBodyException {
        super(md, "S-ParaCount");
    }

    protected <T extends CallableDeclaration<T>> float calculate(T md) {
        NodeList<Parameter> parameters = md.getParameters();
        return parameters.size();
    }
}
