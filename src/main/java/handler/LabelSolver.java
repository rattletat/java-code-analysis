package handler;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;

public class LabelSolver {

    private final JsonReader reader;
    private Map<String, Map<Integer, Set<Map.Entry<String, Integer>>>> relevantLines;

    public LabelSolver(String jsonPath)
    throws JsonIOException, JsonSyntaxException, IOException {
        this.reader = new JsonReader(
            new InputStreamReader(new FileInputStream(jsonPath))
        );

        this.relevantLines = new HashMap<>();
        calculateRelevantLines();

    }

    public List<String[]> getPredictedMethods(CSVHandler suspiciousnessFile,
            List<String[]> patchedMethods) throws IOException {
        List<String[]> suspiciousnessLines = suspiciousnessFile.getData();
        // List<String[]> relevantSuspiciousnessLines = suspiciousnessLines.stream()
        // .filter(array -> array[0])

        return null;
    }

    // Uses the methodLine CSV file (contains file method beginning and ending line) to match it to modified lines
    public List<String[]> getPatchedMethods(String projectName, int versionNumber, CSVHandler methodLineFile) throws IOException {
        List<String[]> data = methodLineFile.getData();
        // Map by file
        Map<String, List<String[]>> fileMethods = data.stream()
                .skip(1)
                .collect(Collectors.groupingBy(line -> line[0]));
        // Get modified, added, deleted line locations
        Set<Map.Entry<String, Integer>> entries = relevantLines
                .get(projectName)
                .get(versionNumber);
        List<String[]> patchedMethods = new LinkedList<>();
        // Iterate through files in project
        for (String file : fileMethods.keySet()) {
            List<String[]> methodRanges = fileMethods.get(file);
            // Iterate through methods in file
            for (String[] method : methodRanges) {
                Double from =  Double.valueOf(method[2]);
                Double to = Double.valueOf(method[3]);
                // get all lines within the method
                Set<Integer> linesInMethod = entries.stream()
                .map(entry -> {
                    return entry;
                })
                .filter(entry -> entry.getKey().equals(file))
                .map(entry -> entry.getValue())
                .filter(i -> from <= i && i <= to)
                .collect(Collectors.toSet());
                if (!linesInMethod.isEmpty()) {
                    patchedMethods.add(method);
                }
            }
        }
        return patchedMethods;
    }

    // For each project and each version, this script extracts information as which lines were deleted, added, modified etc. and saves it for later matching.
    private void calculateRelevantLines() throws IOException {
        reader.beginArray();
        while (reader.hasNext()) {
            reader.beginObject();
            Set<Map.Entry<String, Integer>> possibleModLines = new HashSet<>();
            Set<Map.Entry<String, Integer>> possibleDelLines = new HashSet<>();
            Set<Map.Entry<String, Integer>> possibleInsLines = new HashSet<>();
            String project = "";
            int id = -1;
            while (reader.hasNext()) {

                switch (reader.nextName()) {

                case "bugId":
                    id = reader.nextInt();
                    break;

                case "project":
                    project = reader.nextString();
                    break;

                case "changedFiles":
                    reader.beginObject();
                    while (reader.hasNext()) {
                        String fileName = reader.nextName();
                        reader.beginObject();
                        while (reader.hasNext()) {
                            String operation = reader.nextName();
                            if (operation.equals("changes")) {
                                reader.beginArray();
                                while (reader.hasNext()) {
                                    reader.beginArray();
                                    while (reader.hasNext())
                                        possibleModLines.add(new AbstractMap.SimpleEntry<>(fileName, reader.nextInt()));
                                    reader.endArray();
                                }
                                reader.endArray();
                            } else if (operation.equals("deletes")) {
                                reader.beginArray();
                                while (reader.hasNext()) {
                                    reader.beginArray();
                                    while (reader.hasNext())
                                        possibleDelLines.add(new AbstractMap.SimpleEntry<>(fileName, reader.nextInt()));
                                    reader.endArray();
                                }
                                reader.endArray();
                            } else if (operation.equals("inserts")) {
                                reader.beginArray();
                                while (reader.hasNext()) {
                                    reader.beginArray();
                                    while (reader.hasNext())
                                        possibleInsLines.add(new AbstractMap.SimpleEntry<>(fileName, reader.nextInt()));
                                    reader.endArray();
                                }
                                reader.endArray();
                            }
                        }
                        reader.endObject();
                    }
                    reader.endObject();
                    break;
                default:
                    reader.skipValue();
                }
            }
            if (project.equals("") || id == -1) return;
            Map<Integer, Set<Map.Entry<String, Integer>>> map = this.relevantLines.get(project);
            if (map == null) {
                this.relevantLines.put(project, new HashMap<>());
                map = this.relevantLines.get(project);
            }
            Set<Map.Entry<String, Integer>> lines = map.get(id);
            if (lines == null) {
                map.put(id, new HashSet<>());
                lines = map.get(id);
            }
            lines.addAll(possibleModLines);
            lines.addAll(possibleInsLines);
            lines.addAll(possibleDelLines);
            reader.endObject();
        }
        reader.endArray();
    }
}
