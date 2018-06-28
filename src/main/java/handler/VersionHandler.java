package handler;

import java.util.LinkedList;
import java.util.List;

public class VersionHandler {

    private final String projectName;
    private final int versionNumber;
    private final String dirPath;
    private Integer rows;

    private List<List<String[]>> csvLines;

    public VersionHandler(String projectName, int versionNumber, String dirPath) {
        this.projectName = projectName;
        this.versionNumber = versionNumber;
        this.dirPath = dirPath;
        this.csvLines = new LinkedList<>();
    }

    public void appendColumnLeft(List<String[]> column) {
        checkRowSize(column.size());
        this.csvLines.add(0, column);
    }

    public void appendColumnRight(List<String[]> column) {
        checkRowSize(column.size());
        this.csvLines.add(column);
    }

    private void checkRowSize(int rowSize) {
        if (this.rows == null) {
            this.rows = rowSize;
            return;
        }
        if (this.rows.intValue() != rowSize) {
            throw new IllegalArgumentException("Number of rows is not the same!");
        }
    }
}
