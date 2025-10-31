package pt.axxiv.mariatasks.connection.factory;

public enum TaskFormat {
    ONCE("Once"),
    EVERY_DAY("Every Day"),
    FREQUENCY("Custom Frequency"),
    DATE("Specific Date");

    private final String label;

    TaskFormat(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
