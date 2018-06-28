package staticmetrics.metrics;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import com.github.javaparser.ast.body.CallableDeclaration;

import exceptions.MethodHasNoBodyException;

public class StaticResult {

    private List<String> values;
    private List<String> header;

    public <T extends CallableDeclaration<T>> StaticResult(File file,  T md) throws MethodHasNoBodyException {
        StaticMetric[] staticMetrics = new StaticMetric[] {
            new WordCount(md),
            new LineCount(md),
            new Density(md),
            new ParameterCount(md),
            new FunctionCallCount(md),
            new VariableCount(md),
            new RepeatedWords(md),
            new CommentPercentage(md),
            new MaxDepth(md),
            new BlockCount(md),
            new CyclomaticComplexity(md)
        };

        // Construct header record
        List<String> metricHeader = Arrays.stream(staticMetrics)
                                    .map(metric -> metric.getTag())
                                    .collect(Collectors.toList());
        this.header = new LinkedList<String>() {
            {
                add("File");
                add("Method");
                addAll(metricHeader);
            }
        };

        // Construct value record
        List<String> metricValues = Arrays.stream(staticMetrics)
                                    .map(value -> String.valueOf(value.getValue()))
                                    .collect(Collectors.toList());
        this.values = new LinkedList<String>() {
            {
                add(file.getPath());
                add(cleanSignature(md));
                addAll(metricValues);
            }
        };
    }

    public List<String> getValues() {
        return this.values;

    }
    public List<String> getHeader() {
        return this.header;
    }

    private static String cleanSignature(CallableDeclaration<?> cd) {
        return cd.getSignature().asString()
               .replaceAll(",", "|")
               .replaceAll(" ", "");
    }

    public String toString() {
        String output = "";
        for (int i = 0; i < values.size(); i++) {
            output += this.header.get(i) + ": " + this.values.get(i);
        }
        return output;
    }
}
