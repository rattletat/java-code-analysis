package handler;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Validate;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;

import exceptions.PatchNotInMethodException;

public class LabelSolver {

    private Map<String, Map<Integer, Set<Map.Entry<String, Integer>>>> relevantLines;

    public LabelSolver(String jsonPath)
    throws JsonIOException, JsonSyntaxException, IOException {
        JsonReader reader = new JsonReader(
            new InputStreamReader(new FileInputStream(jsonPath))
        );

        this.relevantLines = new HashMap<>();
        calculateRelevantLines(reader);
        reader.close();
    }

    // Uses the methodLine CSV file (contains file method beginning and ending line) to match it to modified lines
    public Collection<String[]> getPatchedMethods(String projectName, int versionNumber,
            CSVHandler methodLineFile) throws IOException, PatchNotInMethodException {
        List<String[]> data = methodLineFile.getData();
        Validate.isTrue(data.size() >= 1, "No methodLine lines given.");
        // Get modified, added, deleted line locations
        Set<Map.Entry<String, Integer>> entries = relevantLines
                .get(projectName)
                .get(versionNumber);
        Validate.isTrue(entries.size() >= 1, "No modified lines found.");
        for (Map.Entry<String, Integer> entry : entries) {
            // System.out.println("MODIFIED: " + entry.getKey() + " " + entry.getValue());
        }
        Set<String[]> patchedLines = new HashSet<>();
        for (Map.Entry<String, Integer> entry : entries) {
            Integer line = entry.getValue();
            data.stream()
            .skip(1)
            .filter(array -> array[0].equals(entry.getKey()))
            .filter(array -> Double.valueOf(array[2]) <= line && line <= Double.valueOf(array[3]))
            .forEach(array -> patchedLines.add(array));
        }
        if (patchedLines.size() == 0) {
            throw new PatchNotInMethodException();
        }
        return patchedLines;
    }

    // For each project and each version, this script extracts information as which lines were deleted, added, modified etc. and saves it for later matching.
    private void calculateRelevantLines(JsonReader reader) throws IOException {
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

    /**
     * Takes the patched lines from the methodLine file and the
     * lines from the suspiciousness file and returns the minimal rank of all
     * patched lines.
     * @return minimal rank or -1 if method was not in the suspicious file
     * which happens when a method was only added.
     **/
    public Collection<String[]> getFaultyMethods(Collection<String[]> patched_lines,
                                        Collection<String[]> suspicious_methods) {
        Validate.isTrue(patched_lines.size() >= 1, "No patched lines given.");
        Validate.isTrue(suspicious_methods.size() >= 1, "No suspicious lines given.");
        // Change the format of the filepath and group by it
        Map<String, List<String[]>> patched = patched_lines.stream()
        .map(array -> {
            array[0] = array[0].substring(0, array[0].length() - 5); // Remove .java
            array[0] = array[0].replaceAll("/", ".");
            // System.out.println("PATCHED & MODIFED: " + array[0] + " : " + array[2] + " - " + array[3]);
            return array;
        })
        .collect(Collectors.groupingBy(array -> array[0]));

        // From the suspiciousness file get only those lines from
        // patched files
        return suspicious_methods.stream()
               .skip(1) // Skip header
               // .map(line -> { System.out.println("SUS: " + line[0] + " : " + line[1]); return line;})
        .map(line -> { // Remove $... part
            int index = line[0].indexOf('$');
            if (index != -1)
                line[0] = line[0].substring(0, index);
            return line;
        })
        .filter(line -> patched.containsKey(line[0])) // Filter out lines from other files
        .filter(line ->  patched.get(line[0]).stream()
                .anyMatch(p -> Double.valueOf(p[2]) <= Double.valueOf(line[1]) // Check the range
                          && Double.valueOf(line[1]) <= Double.valueOf(p[3])))
        .collect(Collectors.toSet());
    }

    public Integer getMinimalRankLabel(Collection<String[]> relevant_suspicious_methods) throws PatchNotInMethodException {
        // Get the ranks from the filtered suspicious lines
        Collection<Integer> ranks = relevant_suspicious_methods.stream()
                                    .map(line -> line[2])
                                    .map(rank -> Integer.valueOf(rank))
                                    .collect(Collectors.toSet());
        return ranks.stream().min(Integer::compare)
               .orElseThrow(PatchNotInMethodException::new);
    }
}
