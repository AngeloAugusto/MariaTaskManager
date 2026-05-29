package pt.axxiv.mariatasks.data;

public enum TaskFormat {
	ONCE("Once", 1),
    EVERY_DAY("Daily", 2),
    FREQUENCY("Frequency", 5),
    DATE("Date", 6),
    WEEKDAY("Day of the week", 7);

    private final String label;
    private final int value;

    TaskFormat(String label, int value) {
        this.label = label;
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return label; // so UI will show the label by default
    }
}
