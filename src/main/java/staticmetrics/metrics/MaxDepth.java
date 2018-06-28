package staticmetrics.metrics;

import java.util.LinkedList;
import java.util.List;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;

import exceptions.MethodHasNoBodyException;

/**
 * Calculates the depth of a method i.e. the maximal number of nested blocks.
 */
public class MaxDepth extends StaticMetric {

    <T extends CallableDeclaration<T>> MaxDepth(T md) throws MethodHasNoBodyException {
        super(md, "S-MaxDepth");
    }

    protected <T extends CallableDeclaration<T>> float calculate(T md) {
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
