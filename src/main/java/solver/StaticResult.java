package solver;

import java.io.File;

import org.apache.commons.lang3.Validate;

public class StaticResult {

    private File file;
    private String signature;
    private int words;
    private int lines;
    private float density;
    private int arguments;
    private int methodCalls;
    private int variables;
    private int repWords;
    private float commentRatio;
    private int maxDepth;
    private int blocks;

    public StaticResult(
        File file,
        String signature,
        int words,
        int lines,
        float density,
        int arguments,
        int methodCalls,
        int variables,
        int repWords,
        float commentRatio,
        int maxDepth,
        int blocks
    ) {
        this.file = file;
        this.signature = signature;
        this.words = words;
        this.lines = lines;
        this.density = density;
        this.arguments = arguments;
        this.methodCalls = methodCalls;
        this.variables = variables;
        this.repWords = repWords;
        this.commentRatio = commentRatio;
        this.maxDepth = maxDepth;
        this.blocks = blocks;

        Validate.notNull(file, "File can not be null.");
        Validate.notNull(signature, "Signature can not be null.");
        Validate.notNull(words, "Words can not be null.");
        Validate.notNull(lines, "Lines can not be null.");
        Validate.notNull(density, "Density can not be null.");
        Validate.notNull(arguments, "Arguments can not be null.");
        Validate.notNull(methodCalls, "MethodCalls can not be null.");
        Validate.notNull(variables, "Variables can not be null.");
        Validate.notNull(repWords, "RepWords can not be null.");
        Validate.notNull(commentRatio, "CommentRatio can not be null.");
        Validate.notNull(maxDepth, "MaxDepth can not be null.");
        Validate.notNull(blocks, "Blocks can not be null.");
    }

    public String[] getRecord() {
        String[] record = {
            "" + this.file,
            "" + this.signature,
            "" + this.words,
            "" + this.lines,
            "" + this.density,
            "" + this.arguments,
            "" + this.methodCalls,
            "" + this.variables,
            "" + this.repWords,
            "" + this.commentRatio,
            "" + this.maxDepth,
            "" + this.blocks
        };
        return record;
    }

    public String[] getHeader() {
        String[] headerRecord = {
            "FilePath",
            "Signature",
            "Words",
            "Lines",
            "Density",
            "Arguments",
            "MethodCalls",
            "Variables",
            "MaxRepWords",
            "CommentRatio",
            "MaxDepth",
            "Blocks"
        };
        return headerRecord;
    }

    public String toString() {
        String text = "";
        text += "File::signature: " +
                this.file.getPath() + "::" + this.signature + "\n";
        text += "Number of words: " + this.words + "\n";
        text += "Number of lines: " + this.lines + "\n";
        text += "Density: " + this.density + "\n";
        text += "Number of arguments: " + this.arguments + "\n";
        text += "Number of method calls: " + this.methodCalls + "\n";
        text += "Number of variables: " + this.variables + "\n";
        text += "Maximal of repeated words: " + this.repWords + "\n";
        text += "Comment ratio: " + this.commentRatio + "\n";
        text += "Maximum depth: " + this.maxDepth + "\n";
        text += "Number of blocks: " + this.blocks;
        return text;
    }
}
