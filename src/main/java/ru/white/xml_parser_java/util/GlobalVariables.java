package ru.white.xml_parser_java.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class GlobalVariables {
    // Data
    public static final String ROOT_DIRECTORY_PATH = "./";
    public static final String IMAGE_PATH = "/images/photo.png";
    public static final List<String> TARGET_TESTS_TAG_NAMES = List.of("Test", "TestGroup", "SessionAction");
    public static final String VALID_VALUES_UNDEFINED = "Не определено";
    public static final String EMPTY_RESULTS_LABEL = "Общие результаты";
    // Main window
    public static final String MAIN_WINDOW_TITLE = "XML Viewer";
    public static final int[] MAIN_WINDOW_SIZES = new int[] {890, 390};
    public static final String CHOOSE_DIRECTORY_ALERT_MESSAGE = "Не корректно выбрана папка, проверьте правильность выбора.";
    public static final String CHOOSE_FILE_ALERT_MESSAGE = "Не выбран файл, операция не может быть выполнена.";
    public static final String INCORRECT_FILE_DATA_MESSAGE = "Не удалось получить данные из файла.\n Возможно неверная структура XML.";
    public static String getIncorrectFolderPathMessage(String path) { return "Не существующий путь: " + path; }

    // Result window
    public static final int[] RESULT_WINDOW_SIZES = new int[] {900, 500};

    // Edit result group window
    public static final String EDIT_RESULT_GROUP_WINDOW_TITLE = "Редактировать группу результатов";
    public static final int[] EDIT_RESULT_GROUP_WINDOW_SIZES = new int[] {350, 250};
    // Edit result window
    public static final String EDIT_RESULT_WINDOW_TITLE = "Редактировать результат";
    public static final int[] EDIT_RESULT_WINDOW_SIZES = new int[] {350, 450};    // Alert window
    public static final String ALERT_WINDOW_TITLE = "Внимание!";
    public static final int[] ALERT_WINDOW_SIZES = new int[] {400, 150};
    // Graph
    public static final String GRAPH_NODE_NAME = "DecimGraph";
    public static final int[] GRAPH_WINDOW_SIZES = new int[] {500, 300}; // размер графика в PDF (органичен шириной листа A4);
    public static final int GRAPH_WIN_SIZE_COEFFICIENT = 2; // насколько больше размер окна с граффиком в программе, по отношению к PDF.
    public static final String SHOW_GRAPH_BUTTON_TEXT = "Смотреть график";

    // PDF
    public static String getPdfFileName (LocalDate date) { return "Результаты тестов от " + getDate(date) + ".pdf"; }
    public static String getPdfTitleDate (LocalDate date) { return "Тест проведён " + getDate(date); }
    public static final String PDF_TITLE = "Результаты тестов.\n";
    public static String getTableName(String testName, int number, String name) {return testName + " - Таблица № " + number + " - " + name;}
    public static final String TREE_VIEW_COLUMN_NAME = "";
    public static final String VALUE_COLUMN_NAME = "Измерено";
    public static final String VALID_VALUES_COLUMN_NAME = "Допустимое значение";
    public static final String STATUS_COLUMN_NAME = "Статус";
    public static final String PDF_CREATED_MESSAGE = "Файл успешно сохранён";
    public static final String EMPTY_TEST_GROUPS_MESSAGE = "Выбранные тесты не содержат результатов для формирования PDF";

    private static String getDate(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        return formatter.format(date);
    }

    // Instruction window
    public static final String INSTRUCTION_WINDOW_TITLE = "Инструкция";
    public static final int[] INSTRUCTION_WINDOW_SIZES = new int[] {500, 520};
    public static String INSTRUCTION_TEXT = "Инструкция по использованию программы XML Viewer:\n\n" +
            "1 - Главное окно содержит: \n" +
            "- поле ввода выбора папки (активируется по нажатию чекбокса 'Разрешить поиск в других директориях') \n" +
            "- поле ввода для поиска файла в выбранной папке \n" +
            "- окно для просмотра доступных в папке файлов\n" +
            "- кнопку поиска другой паки с файлами и чекбокс для активации этой кнопки\n" +
            "- чекбокс 'Skip UserDefined' при выборе которого в результат не попадают пустые тесты.\n" +
            "- чекбокс 'Skip empty results' при выборе которого в результат не попадают тесты с пустыми результатами.\n" +
            "- выпадающий список с вариантам округления результатов\n" +
            "        -без округления,\n" +
            "        -округление до 2 знаков вверх,\n" +
            "        -округление до 2 десятичных знаков,\n" +
            "        -округление до 3 знаков вверх,\n" +
            "        -округление до 3 десятичных знаков.\n" +
            "- кнопку 'Start' которая запускает чтение файла\n\n" +
            "2 - Окно просмотра результатов состоит из: \n" +
            "- первый ряд вкладок это доступные из файла тесты\n" +
            "- второй ряд вкладок это доступные подтесты, если они есть\n" +
            "- таблица с результатами. Результаты в таблице объеденены в группы, которые можно разворачивать для просмотра конкретных значений\n" +
            "У некоторых групп таких значений нет, а есть только имя и статус, такие строки нельзя развернуть.\n" +
            "Строки с результатами (после разворачивания) имеют справа чекбокс по которому они попадают или не попадают в экспорт, по умолчанию все выбраны \n" +
            "Для редактирования результата необходимо дважды на него нажать.\n" +
            "- в нижней части окна просмотра результатов расположены кнопки экспорта в PDF. Можно экспортировать как один выбранный тест, так и все сразу.\n";
}

