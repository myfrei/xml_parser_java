package ru.white.xml_parser_java.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringManager {
    // Возвращает имя группы тестов.
    public static String getTestGroupName(String fullName) {
        Pattern pattern = Pattern.compile("\\Q.\\E\\d{1,}");
        Matcher matcher = pattern.matcher(fullName);

        List<String> numbersByFullName = new ArrayList<>();
        while (matcher.find()) {
            numbersByFullName.add(matcher.group());
        }

        if (numbersByFullName.isEmpty()) {
            return fullName;
        } else {
            String testNumber = numbersByFullName.get(numbersByFullName.size() - 1)
                    .replaceAll("\\.", "")
                    .replaceAll(" ", "");
            if (testNumber.startsWith("0")) {
                testNumber = testNumber.substring(1);
            }
            return "Тест № " + testNumber;
        }
    }

    public static String removeQuotes(String string) {
        if (string.startsWith("\"") && string.endsWith("\"")) {
            return string.substring(1, string.length() - 1);
        } else if (string.startsWith("\"") && !string.endsWith("\"")) {
            return string.substring(1);
        } else if (!string.startsWith("\"") && string.endsWith("\"")) {
            return string.substring(0, string.length() - 1);
        } else {
            return string;
        }
    }
}
