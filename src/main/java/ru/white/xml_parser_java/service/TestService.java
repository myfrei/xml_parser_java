package ru.white.xml_parser_java.service;

import ru.white.xml_parser_java.model.Test;
import ru.white.xml_parser_java.model.TestResultGroup;
import ru.white.xml_parser_java.util.JsonNodeManager;

import com.fasterxml.jackson.databind.JsonNode;
import ru.white.xml_parser_java.util.StringManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TestService {

    // Возвращает список тестов.
    public List<Test> getTestByTagName(JsonNode node) {
        List<Test> result = new ArrayList<>();
        for (JsonNode testGroupNode : JsonNodeManager.separateUnitedNodes(node)) {
            getTestByNode(testGroupNode).ifPresent(result::add);
        }
        return result;
    }

    // Возвращает тест
    private Optional<Test> getTestByNode(JsonNode testNode) {
        Test result = new Test();
        try {
            if (checkTestOutcomeStatus(testNode)) {
                // Присвоение имени тесту.
                if (testNode.get("callerName") != null) {
                    result.setName(StringManager.removeQuotes(String.valueOf(testNode.get("callerName"))));
                } else {
                    result.setName(StringManager.removeQuotes(String.valueOf(testNode.get("name"))));
                }
                // Получение результатов и присвоение их текущему тесту.
                TestResultGroupService testResultService = new TestResultGroupService();
                List<TestResultGroup> results = testResultService.getTestResultGroups(testNode);
                // Если у теста нет результатов, создаётся специальный один результат помеченный флагом 'single',
                // потом такие результаты будут объединены в один общий тест.
                if (results.isEmpty()) {
                    TestResultGroup singleGroup = new TestResultGroup();
                    singleGroup.setName(result.getName());
                    singleGroup.setStatus(String.valueOf(testNode.get("Outcome").get("value")).replaceAll("\"", ""));
                    singleGroup.setResults(new ArrayList<>());
                    singleGroup.setSelected(true);
                    singleGroup.setEmpty(true);
                    results.add(singleGroup);
                }
                result.setResultGroups(results);
                return Optional.of(result);
            } else {
                return Optional.empty();
            }
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    // Проверяет статус теста.
    private boolean checkTestOutcomeStatus(JsonNode testGroupNode) {
        String outcomeStatus = String.valueOf(testGroupNode.get("Outcome").get("value")).replaceAll("\"", "");
        return outcomeStatus.equals("Passed")
                || outcomeStatus.equals("Done")
                || outcomeStatus.equals("Failed");
    }
}

