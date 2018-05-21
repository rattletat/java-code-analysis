package solver;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.Validate;

import com.opencsv.CSVWriter;

public class FileHandler {

    private File file;
    private Writer writer;

    public FileHandler(String path) throws Exception {
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
        }
        // Create buffered file writer.
        try {
            this.writer = Files.newBufferedWriter(Paths.get(path), StandardOpenOption.APPEND);
        } catch (Exception e) {
            throw new Exception("Writer could not be created.");
        }
    }

    protected static List<File> getSubfolderClasses(String path) {
        File rootFile = new File(path);
        List<File> files = new LinkedList<File>();
        if (rootFile.isDirectory()) {
            files = (List<File>) FileUtils.listFiles(rootFile, new String[] { "java" }, true);
        } else if (rootFile.isFile()) {
            files.add(rootFile);
        } else {
            System.err.println("File path not correct.");
            System.exit(1);
        }
        return files;
    }

    protected void writeCSVFile(String[] header, String[] args) throws IOException {
        boolean fileExists = this.file.isFile();
        try (
                CSVWriter csvWriter = new CSVWriter(this.writer,
                                                    CSVWriter.DEFAULT_SEPARATOR,
                                                    CSVWriter.NO_QUOTE_CHARACTER,
                                                    CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                                                    CSVWriter.DEFAULT_LINE_END); ) {
            if (!fileExists) csvWriter.writeNext(header);
            csvWriter.writeNext(args);
        }
    }

    protected void close() throws IOException {
        this.writer.close();
    }
}
