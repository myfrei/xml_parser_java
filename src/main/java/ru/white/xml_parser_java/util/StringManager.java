package ru.white.xml_parser_java.util;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.Arrays;
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

    public static String getOriginName(String callerName) {
        // Разделяем строку по пробелам
        String[] parts = callerName.split(" ");

        // Проверяем, что частей больше одной
        if (parts.length > 1) {
            // Объединяем все части начиная со второй (индекс 1)
            String originName = String.join(" ", Arrays.copyOfRange(parts, 1, parts.length));

            // Убираем кавычку в конце, если она есть
            if (originName.endsWith("\"")) {
                originName = originName.replaceAll("\"$", "");
            }

            return originName;
        } else {
            // Если вдруг строка состоит только из одной части, возвращаем ее
            return callerName.replaceAll("\"$", "");
        }
    }

    public static String getStateType(JsonNode testGroupNode) {
        JsonNode jsonNode = testGroupNode.get("Extension");
        if (jsonNode != null) {
            return String.valueOf(jsonNode.get("TSStepProperties").get("StepType"));
        }
        return null;
    }
}
