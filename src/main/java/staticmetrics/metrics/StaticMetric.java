package staticmetrics.metrics;

import com.github.javaparser.ast.body.MethodDeclaration;

import staticmetrics.MethodHasNoBodyException;

abstract class StaticMetric {

    private String tag;
    private float value;

    protected StaticMetric(MethodDeclaration md, String tag) throws MethodHasNoBodyException {
        if (!md.getBody().isPresent()) {
            throw new MethodHasNoBodyException();
        }
        this.tag = tag;
        this.value = calculate(md);
    }

    protected abstract float calculate(MethodDeclaration md);

    public float getValue() {
        return this.value;
    }

    public String getTag(){
        return this.tag;
    }

    public String toString() {
        return String.format("%s: %.4f", this.tag, this.value);
    }

}
