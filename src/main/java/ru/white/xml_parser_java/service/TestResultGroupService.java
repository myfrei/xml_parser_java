package ru.white.xml_parser_java.service;

import com.fasterxml.jackson.databind.JsonNode;
import ru.white.xml_parser_java.model.TestResult;
import ru.white.xml_parser_java.model.TestResultGroup;
import ru.white.xml_parser_java.util.GlobalVariables;
import ru.white.xml_parser_java.util.JsonNodeManager;
import ru.white.xml_parser_java.util.StatusType;
import ru.white.xml_parser_java.util.StringManager;

import java.util.ArrayList;
import java.util.List;

public class TestResultGroupService {
    // Возвращает группы результатов теста.
    public List<TestResultGroup> getTestResultGroups(JsonNode testNode) {
        List<TestResultGroup> result = new ArrayList<>();
        GlobalVariables.TARGET_TESTS_TAG_NAMES.forEach(tagName -> {
            JsonNode testResultGroupsNode = testNode.get(tagName);
            if (testResultGroupsNode != null) {
                for (JsonNode testResultGroupNode : JsonNodeManager.separateUnitedNodes(testResultGroupsNode)) {
                    TestResultGroup testResultGroup = new TestResultGroup();
                    // Присвоение имени группе результатов.
                    if (testResultGroupNode.get("callerName") != null) {
                        testResultGroup.setName(StringManager.removeQuotes(String.valueOf(testResultGroupNode.get("callerName"))));
                    } else {
                        testResultGroup.setName(StringManager.removeQuotes(String.valueOf(testResultGroupNode.get("name"))));
                    }
                    // Присвоение статуса группе результатов.
                    testResultGroup.setStatus(StatusType.fromString(StringManager.removeQuotes(JsonNodeManager.getStatus(testResultGroupNode))).getRussianTranslation());
                    // Флаг 'selected' устанавливается в активное положение. (чекбокс в табице выбран)
                    testResultGroup.setSelected(true);
                    // Получение результатов и присвоение их группе.
                    TestResultService testResultService = new TestResultService();
                    testResultGroup.setResults(testResultService.getTestResults(testResultGroupNode));
                   // System.out.println(testResultGroup);

                    // Проверяет есть ли у группы результатов график. Строит и присваивает его если он есть.
                    if (testResultGroup.getName().contains(GlobalVariables.GRAPH_NODE_NAME)) {
                        GraphService graphService = new GraphService();
                        testResultGroup.setGraph(graphService.getGraph(testResultGroupNode));
                    }

                    if (testResultGroup.getResults().isEmpty() && testResultGroup.getGraph() == null) {
                        testResultGroup.setResults(testResultService.getTestResultsFromSessionAction(testResultGroupNode));
                    }
                    // Добавление группы результатов в результирующий список.
                    result.add(testResultGroup);
                }
            }
        });
        //return mergeSameNamesTestResultGroups(result);
        return result;
    }

    // Объединяет группы результатов с одинаковыми именами.
    public List<TestResultGroup> mergeSameNamesTestResultGroups(List<TestResultGroup> testResultGroups) {
        List<TestResultGroup> result = new ArrayList<>();
        for (TestResultGroup testResultGroup : testResultGroups) {
            TestResultGroup sameTestResultGroup = result
                    .stream()
                    .filter(trg -> trg.getName().equals(testResultGroup.getName()))
                    .findFirst()
                    .orElse(null);
            if (sameTestResultGroup == null) {
                result.add(testResultGroup);
            } else {
                List<TestResult> newTestResultList = new ArrayList<>();
                newTestResultList.addAll(sameTestResultGroup.getResults());
                newTestResultList.addAll(testResultGroup.getResults());
                sameTestResultGroup.setResults(newTestResultList);
            }
        }
        return result;
    }
}

