package ru.white.xml_parser_java.model;

import ru.white.xml_parser_java.util.StringManager;

import java.util.Optional;

public enum LimitComparator {
    GT (">"),
    GTE (">="),
    LT ("<"),
    LE ("≤"),
    GE("≥"),
    NE("≠"),
    LTE ("<=");

    private final String viewValue;

    LimitComparator(String stringValue) {
        this.viewValue = stringValue;
    }

    public String getViewValue() {
        return viewValue;
    }

    public static Optional<LimitComparator> getByStringValue(String value) {
        for (LimitComparator lc : LimitComparator.values()) {
            if (lc.toString().equals(StringManager.removeQuotes(value))) {
                return Optional.of(lc);
            }
        }
        return Optional.empty();
    }
}
