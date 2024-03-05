package ru.white.xml_parser_java.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.util.ArrayList;
import java.util.List;

public class JsonNodeManager {
    // Разделяет и собирает как одиночные, так и пришедшие группой узлы в один список.
    public static List<JsonNode> separateUnitedNodes(JsonNode node) {
        List<JsonNode> result = new ArrayList<>();
        // Вложенный список ArrayNode это особенность реализации jackson-databind и к вложенным тестам из файла отношения не имеет.
        if (node.getClass().equals(ArrayNode.class)) {
            for (JsonNode innerNode : node) {
                result.add(innerNode);
            }
        } else {
            result.add(node);
        }
        return result;
    }


    // Возвращает статус узла.
    public static String getStatus(JsonNode testResultNode) {
        if (testResultNode.get("Outcome") != null) {
            return String.valueOf(testResultNode.get("Outcome").get("value"));
        } else if (testResultNode.get("ActionOutcome") != null) {
            return String.valueOf(testResultNode.get("ActionOutcome").get("value"));
        } else {
            return "N/A";
        }
    }
}

