package staticmetrics.metrics;

import java.util.Arrays;
import java.util.LinkedList;

import com.github.javaparser.ast.body.MethodDeclaration;

import staticmetrics.MethodHasNoBodyException;

public class StaticResult {

    private StaticMetric[] staticMetrics;
    private String[] valueRecord;
    private String[] headerRecord;

    public StaticResult(MethodDeclaration md) throws MethodHasNoBodyException {
        this.staticMetrics = new StaticMetric[] {
            new WordCount(md),
            new LineCount(md),
            new Density(md),
            new ParameterCount(md),
            new FunctionCallCount(md),
            new VariableCount(md),
            new RepeatedWords(md),
            new CommentPercentage(md),
            new MaxDepth(md),
            new BlockCount(md)
        };

        System.out.println(this.staticMetrics);

        // Generate ValueRecord
        this.valueRecord = Arrays.stream(this.staticMetrics).map(
                               metric -> String.valueOf(metric.getValue())
                           ).toArray(String[]::new);

        // Generate HeaderRecord
        this.headerRecord = Arrays.stream(this.staticMetrics).map(
                                metric -> metric.getTag()
                            ).toArray(String[]::new);
    }

    public LinkedList<String> getValueRecord() {
        return new LinkedList<String>(Arrays.asList(this.valueRecord));
    }

    public LinkedList<String> getHeaderRecord() {
        return new LinkedList<String>(Arrays.asList(this.headerRecord));
    }

    public String toString() {
        return Arrays.stream(this.staticMetrics).map(
                   metric -> metric.toString() + "\n"
               ).reduce("", String::concat);
    }
}
