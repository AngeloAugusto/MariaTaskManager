package pt.axxiv.mariatasks.data;

public enum TaskFormat {
	ONCE("Uma vez", 1),
    EVERY_DAY("Diária", 2),
    FREQUENCY("Frequência", 5),
    DATE("Data", 6);

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
