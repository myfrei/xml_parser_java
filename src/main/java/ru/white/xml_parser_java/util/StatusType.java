package ru.white.xml_parser_java.util;

public enum StatusType {
    FAILED("Ошибка"),
    PASSED("Без замечаний"),
    SKIPPED("Пропущено"),
    DONE("Выполнено"),
    ABORTED("Ошибка"),
    USER_DEFINE("Ошибка"),

    NONE("''");

    private final String russianTranslation;

    // Конструктор для инициализации русского значения
    StatusType(String russianTranslation) {
        this.russianTranslation = russianTranslation;
    }

    // Геттер для получения русского значения
    public String getRussianTranslation() {
        return russianTranslation;
    }

    // Статический метод для поиска enum по строке на английском
    public static StatusType fromString(String text) {
        for (StatusType status : StatusType.values()) {
            if (status.name().equalsIgnoreCase(text)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown status: " + text);
    }
}
