package pt.axxiv.mariatasks.data;

public enum SectionType {
    TASKS     ("Tasks",       1),
    BIRTHDAYS  ("Birthdays",   2),
    SHOPPING   ("Shopping",    3),
    MECHANIC   ("Mechanic",    4),
    COLLECTIONS("Collections", 5),
    VIDEOGAMES ("Video Games", 6);

    private final String label;
    private final int value;

    SectionType(String label, int value) {
        this.label = label;
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public int getValue() {
        return value;
    }

    public static SectionType fromValue(int value) {
        for (SectionType st : values()) {
            if (st.getValue() == value) return st;
        }
        throw new IllegalArgumentException("Unknown value: " + value);
    }

    @Override
    public String toString() {
        return label; // UI (combobox, labels) shows the human-friendly name
    }
}