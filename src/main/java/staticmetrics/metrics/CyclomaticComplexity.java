package staticmetrics.metrics;

import java.util.List;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.SwitchEntryStmt;
import com.github.javaparser.ast.stmt.WhileStmt;

import staticmetrics.MethodHasNoBodyException;

/**
 * Counts the amount of local variables in a method.
 */
public class CyclomaticComplexity extends StaticMetric {

    CyclomaticComplexity(MethodDeclaration md) throws MethodHasNoBodyException {
        super(md, "S-CycCom");
    }

    // https://www.theserverside.com/feature/How-to-calculate-McCabe-cyclomatic-complexity-in-Java
    protected float calculate(MethodDeclaration md) {
        int conditionalCount = md.findAll(IfStmt.class).size();
        int forCount = md.findAll(ForStmt.class).size();
        int whileCount = md.findAll(WhileStmt.class).size();
        int switchCount = md.findAll(SwitchEntryStmt.class).size();
        int binaryExprCount = md.findAll(BinaryExpr.class).size();
        int sum = conditionalCount + forCount + whileCount + switchCount + binaryExprCount;
        return sum;
    }
}
