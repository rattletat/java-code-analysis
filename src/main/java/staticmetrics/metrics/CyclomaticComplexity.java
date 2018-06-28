package staticmetrics.metrics;

import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.SwitchEntryStmt;
import com.github.javaparser.ast.stmt.WhileStmt;

import exceptions.MethodHasNoBodyException;

/**
 * Counts the amount of local variables in a method.
 */
public class CyclomaticComplexity extends StaticMetric {

    <T extends CallableDeclaration<T>> CyclomaticComplexity(T md) throws MethodHasNoBodyException {
        super(md, "S-CycCom");
    }

    // https://www.theserverside.com/feature/How-to-calculate-McCabe-cyclomatic-complexity-in-Java
    protected <T extends CallableDeclaration<T>> float calculate(T md) {
        int conditionalCount = md.findAll(IfStmt.class).size();
        int forCount = md.findAll(ForStmt.class).size();
        int whileCount = md.findAll(WhileStmt.class).size();
        int switchCount = md.findAll(SwitchEntryStmt.class).size();
        int binaryExprCount = md.findAll(BinaryExpr.class).size();
        int sum = conditionalCount + forCount + whileCount + switchCount + binaryExprCount;
        return sum;
    }
}
