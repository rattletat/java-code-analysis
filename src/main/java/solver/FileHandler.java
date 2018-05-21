package solver;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.Validate;

import com.opencsv.CSVWriter;

public class FileHandler {

    private File file;

    public FileHandler(String path) {
        this.file = new File(path);
        // Check whether path is valid.
        if (!file.exists()) {
            try {
                file.createNewFile();
                file.delete();
            } catch (Exception e) {
                System.err.println("Given path is invalid.");
                System.exit(1);
            }
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
            System.out.println("File path not correct.");
            System.exit(1);
        }
        return files;
    }

    protected void writeCSVFile(String[] header, String[] args) {
        Validate.isTrue(!file.isDirectory(), "Specified path is a directory.");

        boolean fileExists = this.file.isFile();
        try (
                // Writer writer = Files.newBufferedWriter(Paths.get(path), StandardOpenOption.APPEND);
                Writer writer = new FileWriter(file, fileExists);
                CSVWriter csvWriter = new CSVWriter(writer,
                                                    CSVWriter.DEFAULT_SEPARATOR,
                                                    CSVWriter.NO_QUOTE_CHARACTER,
                                                    CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                                                    CSVWriter.DEFAULT_LINE_END); ) {
            if (!fileExists) csvWriter.writeNext(header);
            csvWriter.writeNext(args);
        } catch (IOException e) {
            System.err.println("Error during CSV file generation! Aborting ...");
            System.out.println(e);
        }
    }
}
