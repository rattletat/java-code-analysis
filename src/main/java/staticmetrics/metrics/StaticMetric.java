package staticmetrics.metrics;

import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.nodeTypes.NodeWithBlockStmt;
import com.github.javaparser.ast.nodeTypes.NodeWithOptionalBlockStmt;
import com.github.javaparser.ast.stmt.BlockStmt;

import exceptions.MethodHasNoBodyException;

abstract class StaticMetric {

    private String tag;
    private float value;

    protected <T extends CallableDeclaration<T>> StaticMetric(T md, String tag) throws MethodHasNoBodyException {
        if (md instanceof NodeWithOptionalBlockStmt && !((NodeWithOptionalBlockStmt<T>) md).getBody().isPresent()) {
            throw new MethodHasNoBodyException();
        }
        this.tag = tag;
        this.value = calculate(md);
    }

    protected abstract <T extends CallableDeclaration<T>> float calculate(T md);

    protected <T extends CallableDeclaration<T>> BlockStmt getBlock(T cd) {
        BlockStmt block;
        if (cd instanceof NodeWithOptionalBlockStmt) {
            block = ((NodeWithOptionalBlockStmt<T>) cd).getBody().get();
        } else if (cd instanceof NodeWithBlockStmt) {
            block = ((NodeWithBlockStmt<T>) cd).getBody();
        } else throw new RuntimeException(); // Case is handled in constructor
        return block;
    }


    public float getValue() {
        return this.value;
    }

    public String getTag() {
        return this.tag;
    }

    public String toString() {
        return String.format("%s: %.4f", this.tag, this.value);
    }

}
