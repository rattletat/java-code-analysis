package staticmetrics.metrics;

import java.io.File;
import java.util.Arrays;
import java.util.stream.Collectors;

import com.github.javaparser.ast.body.MethodDeclaration;

import staticmetrics.MethodHasNoBodyException;
// TODO: Refactor to StaticMethodResult, add StaticFileResult, add Method Count, File size, number of constants etc.
// Fix deprecated with java 8 streams

public class StaticResult {

    private StaticMetric[] staticMetrics;
    private String values;
    private String header;

    public StaticResult(File file, MethodDeclaration md) throws MethodHasNoBodyException {
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

        this.values = Arrays.stream(this.staticMetrics)
                      .map(value -> String.valueOf(value.getValue()))
                      .collect(Collectors.joining(","));
        this.values = String.join(",", file.getPath(), cleanSignature(md), this.values);

        this.header = Arrays.stream(this.staticMetrics)
                      .map(metric -> metric.getTag())
                      .collect(Collectors.joining(","));
        this.header = "File,Method," + this.header;
    }

    public String getValues() {
        return this.values;

    }
    public String getHeader() {
        return this.header;
    }

    private static String cleanSignature(MethodDeclaration md) {
        String result = md.getSignature().asString();
        return result.replaceAll(",", "|").replaceAll(" ", "");
    }

    public String toString() {
        return Arrays.stream(this.staticMetrics).map(
                   metric -> metric.toString() + "\n"
               ).reduce("", String::concat);
    }
}
