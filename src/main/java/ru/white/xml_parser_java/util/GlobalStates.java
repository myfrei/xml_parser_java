package ru.white.xml_parser_java.util;

import ru.white.xml_parser_java.model.RoundingOptionals;

// Глобальные состояния
public class GlobalStates {

    // Показывать или нет пустые группы тестов
    private static boolean userDefined = true;
    public static boolean isUserDefined() {
        return userDefined;
    }
    public static void setUserDefined(boolean userDefined) {
        GlobalStates.userDefined = userDefined;
    }
    // Показывать или нет пустые результаты
    private static boolean showEmptyResults = true;
    public static boolean isShowEmptyResults() {
        return showEmptyResults;
    }
    public static void setShowEmptyResults(boolean showEmptyResults) {
        GlobalStates.showEmptyResults = showEmptyResults;
    }
    // Как округлять результаты
    private static RoundingOptionals roundingOptional = RoundingOptionals.NO_ROUND; // по умолчанию не округляет.
    public static RoundingOptionals getRoundingOptional() {
        return roundingOptional;
    }
    public static void setRoundingOptional(RoundingOptionals roundingOptional) {
        GlobalStates.roundingOptional = roundingOptional;
    }
}

