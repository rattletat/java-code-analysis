package util;

public final class ProjectVersion {
    public final String projectName;
    public final Integer version;

    public ProjectVersion(String projectName, Integer version) {
        this.projectName = projectName;
        this.version = version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (this.getClass() != o.getClass()) {
            return false;
        }
        ProjectVersion pv = (ProjectVersion) o;
        return this.projectName.equals(pv.projectName)
               && this.version.equals(pv.version);
    }

    @Override
    public int hashCode() {
        return this.projectName.hashCode() + this.version + 13;
    }
}
