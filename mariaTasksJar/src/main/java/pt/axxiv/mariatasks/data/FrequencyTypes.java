package pt.axxiv.mariatasks.data;

public enum FrequencyTypes {
	BY_DAY("De X em X dias", 1),
    BY_MONTH("De X em X meses", 2),
    BY_YEAR("De X em X anos", 3);

    private final String label;
    private final int value;

    FrequencyTypes(String label, int value) {
        this.label = label;
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public int getValue() {
        return value;
    }
    
    public static FrequencyTypes fromValue(int value) {
        for (FrequencyTypes ft : values()) {
            if (ft.getValue() == value) return ft;
        }
        throw new IllegalArgumentException("Unknown value: " + value);
    }

    @Override
    public String toString() {
        return label; // so UI will show the label by default
    }

}
