package solver;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.LinkedList;

import org.apache.commons.lang3.Validate;

import com.opencsv.CSVWriter;

public class CSVHandler {

    private File file;
    boolean fileExists;
    private CSVWriter writer;

    public CSVHandler(String path) throws Exception {
        this.file = new File(path);
        Validate.isTrue(!file.isDirectory(), "Specified path is a directory.");
        // Check whether path is valid.
        if (!file.exists()) {
            try {
                file.createNewFile();
                file.delete();
            } catch (Exception e) {
                throw new Exception("File not creatable.");
            }
        } else {
            file.delete();
        }
        this.fileExists = false;
        // Create buffered file writer.
        try {
            Writer BufWriter;
            if (file.exists()) {
                BufWriter = Files.newBufferedWriter(Paths.get(path), StandardOpenOption.APPEND);
            } else {
                BufWriter = Files.newBufferedWriter(Paths.get(path), StandardOpenOption.CREATE);
            }
            this.writer = new CSVWriter(BufWriter,
                                        CSVWriter.DEFAULT_SEPARATOR,
                                        CSVWriter.NO_QUOTE_CHARACTER,
                                        CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                                        CSVWriter.DEFAULT_LINE_END);
        } catch (Exception e) {
            throw new Exception("Writer could not be created.");
        }
    }

    public void writeCSVFile(LinkedList<String> header, LinkedList<String> args) throws IOException {
        String[] headerArray = header.toArray(new String[header.size()]);
        String[] argsArray = args.toArray(new String[args.size()]);
        if (!this.fileExists) {
            this.writer.writeNext(headerArray);
            this.fileExists = true;
        }
        this.writer.writeNext(argsArray);
        System.out.println("Writing to: " + this.file.getPath());
    }

    public void close() throws IOException {
        this.writer.close();
    }
}
