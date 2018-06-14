package handler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.Validate;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

public class CSVHandler {

    private File file;
    private CSVWriteHandler writer;

    public CSVHandler(String path, boolean deleteOld) throws Exception {
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
        } else if (deleteOld) {
            file.delete();
        }
    }

    public class CSVWriteHandler {
        private CSVWriter writer;

        public CSVWriteHandler(String path, List<String> header) throws IOException {
            this.writer = this.getNewCSVWriter(path);
            writeLine(header);
        }

        public void writeLine(List<String> lineElements) throws IOException {
            String[] lineArray = lineElements.toArray(new String[lineElements.size()]);
            this.writer.writeNext(lineArray);
            this.writer.flush();
        }

        public CSVWriter getNewCSVWriter(String path) throws IOException {
            Writer BufWriter = Files.newBufferedWriter(Paths.get(path), StandardOpenOption.CREATE);
            return new CSVWriter(BufWriter,
                                 CSVWriter.DEFAULT_SEPARATOR,
                                 CSVWriter.NO_QUOTE_CHARACTER,
                                 CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                                 CSVWriter.DEFAULT_LINE_END);
        }

        public void close() throws IOException {
            this.writer.close();
        }
    }

    public void appendLeft(String header, List<String> data) { 
        List<String> resultLines = new LinkedList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file.getPath()));
            String line = br.readLine();
            resultLines.add(header + "," + line);
            int i = 0;
            while ((line = br.readLine()) != null) {
                resultLines.add(data.get(i) + "," + line);
                i++;
            }
            br.close();
            Validate.isTrue(data.size() == i, "Column size and CSV File row size are different!\nColumn: " + (data.size()+1) + " CSV File: " + (i+1));
            this.close();
            FileWriter newwriter = new FileWriter(file.getPath());
            for (String newline : resultLines) {
                newwriter.write(newline + "\n");
                newwriter.flush();
            }
            this.writer = null;
            newwriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<String[]> getData() throws IOException {
        Reader reader = new FileReader(this.file);
        CSVReader csvReader = new CSVReader(reader);
        List<String[]> data = csvReader.readAll();
        csvReader.close();
        return data;
    }

    public void writeLines(List<String> header, List<List<String>> lines) throws IOException {
        for (List<String> line : lines) {
            writeLine(header, line);
        }
    }

    public void writeLine(List<String> header, List<String> line) throws IOException {
        if (this.writer == null) {
            this.writer = new CSVWriteHandler(this.file.getPath(), header);
        }
        this.writer.writeLine(line);
    }

    public void close() throws IOException {
        if (this.writer != null) {
            writer.close();
        }
    }

    // Takes mutiple CSV Files and glue them horizontically together.
    public void combineCSVFiles(List<CSVHandler> csvFiles) throws IOException {

        List<List<String[]>> fileList = new LinkedList<>();
        for (CSVHandler csvHandler : csvFiles) {
            fileList.add(csvHandler.getData());
        }
        boolean allEqualSize = fileList.stream()
                               .map(lineList -> lineList.size())
                               .distinct().count() == 1;
        if (!allEqualSize) {
            throw new IllegalArgumentException("[ERROR] Combining CSV Files failed due to files of different vertical length.");
        }

        // Map List<List<String[]>> to List<List<List<String>>>
        Stream<List<List<String>>> listValues = fileList.stream()
                                                .map(lineList -> lineList.stream()
                                                        .map(valueList -> Arrays.asList(valueList))
                                                        .collect(Collectors.toList()));

        List<List<String>> resultLines = new LinkedList<>();
        // Append each line to result lines
        listValues.forEach(lineList -> {
            for (int i = 0; i < lineList.size() ; i++) {
                if (resultLines.size() <= i) {
                    resultLines.add(new LinkedList<String>());
                }
                resultLines.get(i).addAll(lineList.get(i));
            }
        });
        List<String> header = resultLines.remove(0);

        this.writeLines(header, resultLines);
        this.close();
    }

    public void stackCSVFiles(List<CSVHandler> csvFiles) throws IOException {
        List<List<String[]>> fileList = new LinkedList<>();
        List<String> header;
        for (CSVHandler csvHandler : csvFiles) {
            fileList.add(csvHandler.getData());
        }
        header = Arrays.asList(fileList.get(0).get(0));
        List<List<String>> resultLines = fileList.stream()
                                         .map(file -> {file.remove(0); return file;})
                                         .flatMap(List::stream)
                                         .map(lineArray -> Arrays.asList(lineArray))
                                         .collect(Collectors.toList());
        this.writeLines(header, resultLines);
    }
}
