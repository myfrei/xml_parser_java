package ru.white.xml_parser_java.model;

import java.util.ArrayList;
import java.util.List;

public enum RoundingOptionals {
    NO_ROUND ("Без округления"),
    TWO_UP ("До 2ух знаков в большую сторону"),
    TWO_DOWN ("До 2ух знаков в меньшую сторону"),
    THREE_UP ("До 3ох знаков в большую сторону"),
    THREE_DOWN ("До 3ох знаков в меньшую сторону");

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

