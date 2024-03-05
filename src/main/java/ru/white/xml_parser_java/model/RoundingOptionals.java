package ru.white.xml_parser_java.model;

import java.util.ArrayList;
import java.util.List;

public enum RoundingOptionals {
    NO_ROUND ("No rounding"),
    TWO_UP ("Up to 2 characters upward"),
    TWO_DOWN ("Up to 2 decimal places"),
    THREE_UP ("Up to 3 characters upward"),
    THREE_DOWN ("Up to 3 decimal places");

    private final String title;

    RoundingOptionals(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    // Возвращает все возможные опции в виде списка строк (для выпадающего списка)
    public static List<String> getListOfRoundingOptions() {
        List<String> result = new ArrayList<>();
        for (RoundingOptionals option : RoundingOptionals.values()) {
            result.add(option.getTitle());
        }
        return result;
    }

    // Возвращает опцию по её названию (при выборе в выпадающем списке)
    public static RoundingOptionals getByTitle(String title) {
        for (RoundingOptionals option : RoundingOptionals.values()) {
            if (title.equals(option.getTitle())) return option;
        }
        return NO_ROUND;
    }
}

