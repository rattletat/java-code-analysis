package staticmetrics.metrics;

import java.util.LinkedList;
import java.util.List;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;

import staticmetrics.MethodHasNoBodyException;

/**
 * Calculates the depth of a method i.e. the maximal number of nested blocks.
 */
public class MaxDepth extends StaticMetric {

    MaxDepth(MethodDeclaration md) throws MethodHasNoBodyException {
        super(md, "S-MaxDepth");
    }

    protected float calculate(MethodDeclaration md) {
        List<Node> frontier = new LinkedList<Node>();
        frontier.add(md);
        int depth = 0;
        while (!frontier.isEmpty()) {
            List<Node> children = new LinkedList<Node>();
            for (Node node : frontier) {
                children.addAll(node.findAll(BlockStmt.class));
                children.remove(node);
            }
            frontier.clear();
            if (!children.isEmpty()) {
                frontier.addAll(children);
                depth++;
            }
        }
        return depth;
    }
}
