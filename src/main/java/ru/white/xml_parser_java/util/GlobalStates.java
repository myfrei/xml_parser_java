package ru.white.xml_parser_java.util;

import ru.white.xml_parser_java.model.RoundingOptionals;

import java.math.BigDecimal;

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

    // Парсит строку и округляет согласно состоянию 'roundingOptional' и возвращает значение.
    public static String getRoundedValue(String value) {
        if (!roundingOptional.equals(RoundingOptionals.NO_ROUND)) {
            try {
                double doubleValue = Double.parseDouble(value);
                BigDecimal bigDecimal = new BigDecimal(doubleValue);
                switch (roundingOptional) {
                    case TWO_UP:
                        return String.valueOf(bigDecimal.setScale(2, BigDecimal.ROUND_UP));
                    case TWO_DOWN:
                        return String.valueOf(bigDecimal.setScale(2, BigDecimal.ROUND_DOWN));
                    case THREE_UP:
                        return String.valueOf(bigDecimal.setScale(3, BigDecimal.ROUND_UP));
                    case THREE_DOWN:
                        return String.valueOf(bigDecimal.setScale(3, BigDecimal.ROUND_DOWN));
                    default:
                        return String.valueOf(doubleValue);
                }
            } catch (Exception ex) {
                return value;
            }
        } else {
            return value;
        }
    }
}

