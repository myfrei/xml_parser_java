package ru.white.xml_parser_java.service;

import com.fasterxml.jackson.databind.JsonNode;
import ru.white.xml_parser_java.model.Test;
import ru.white.xml_parser_java.model.TestGroup;
import ru.white.xml_parser_java.util.GlobalStates;
import ru.white.xml_parser_java.util.GlobalVariables;
import ru.white.xml_parser_java.util.JsonNodeManager;
import ru.white.xml_parser_java.util.StringManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TestGroupService {

    // Возвращает список групп тестов.
    public List<TestGroup> getTestGroupsByTagName(JsonNode node) {
        List<TestGroup> result = new ArrayList<>();
        for (JsonNode testGroupNode : JsonNodeManager.separateUnitedNodes(node)) {
            getTestGroupByNode(testGroupNode).ifPresent(result::add);
        }
        return result;
    }

    // Получает TestGroup из JsonNode.
    private Optional<TestGroup> getTestGroupByNode(JsonNode testGroupNode) {
        TestGroup result = new TestGroup();
        try {
            if (checkTestGroupOutcomeStatus(testGroupNode)) {
                // Присвоение имени группе тестов.
                if (testGroupNode.get("callerName") != null) {
                    result.setName(StringManager.getTestGroupName(String.valueOf(testGroupNode.get("callerName"))));
                } else {
                    result.setName(StringManager.getTestGroupName(String.valueOf(testGroupNode.get("name"))));
                }
                // Получение вложенных тестов и присвоение их текущей группе.
                List<Test> tests = new ArrayList<>();
                GlobalVariables.TARGET_TESTS_TAG_NAMES.forEach(tagName -> {
                    JsonNode currentTagNode = testGroupNode.get(tagName);
                    if (currentTagNode != null) {
                        TestService testService = new TestService();
                        List<Test> testsByTagName = testService.getTestByTagName(currentTagNode);
                        if (!testsByTagName.isEmpty()) {
                            tests.addAll(testsByTagName);
                        }
                    }
                });
                result.setTests(tests);
                return Optional.of(result);
            } else {
                return Optional.empty();
            }
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    // Проверяет статус групп тестов.
    private boolean checkTestGroupOutcomeStatus(JsonNode testGroupNode) {
        String outcomeStatus = String.valueOf(testGroupNode.get("Outcome").get("value")).replaceAll("\"", "");
        return outcomeStatus.equals("Passed")
                || outcomeStatus.equals("Failed")
                || (GlobalStates.isUserDefined() && outcomeStatus.equals("UserDefined"));
    }
}

