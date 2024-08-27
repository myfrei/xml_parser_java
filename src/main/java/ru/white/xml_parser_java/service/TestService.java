package ru.white.xml_parser_java.service;

import ru.white.xml_parser_java.model.Test;
import ru.white.xml_parser_java.model.TestResultGroup;
import ru.white.xml_parser_java.util.GlobalVariables;
import ru.white.xml_parser_java.util.JsonNodeManager;

import com.fasterxml.jackson.databind.JsonNode;
import ru.white.xml_parser_java.util.StatusType;
import ru.white.xml_parser_java.util.StringManager;

import java.util.ArrayList;
import java.util.Collections;
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
                    TestResultService singleTestResulService = new TestResultService();
                    singleGroup.setName(result.getName());
                    singleGroup.setStageType(StringManager.getStateType(testNode));
                    singleGroup.setStatus(StatusType.fromString(StringManager.removeQuotes(JsonNodeManager.getStatus(testNode))).getRussianTranslation());
                    singleGroup.setResults(singleTestResulService.getTestResults(testNode));
                    singleGroup.setSelected(true);
                    singleGroup.setEmpty(singleGroup.getResults().isEmpty());
                    results.add(singleGroup);
                    if (singleGroup.getName().contains(GlobalVariables.GRAPH_NODE_NAME)) {
                        GraphService graphService = new GraphService();
                        singleGroup.setGraph(graphService.getGraph(testNode));
                    }
                    if (singleGroup.getResults().isEmpty() && singleGroup.getGraph() == null) {
                        singleGroup.setResults(singleTestResulService.getTestResultsFromSessionAction(testNode));
                    }
                }
                result.setResultGroups(results);
                return Optional.of(result);
            } else {
                return Optional.empty();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return Optional.empty();
        }
    }

    // Проверяет статус теста.
    private boolean checkTestOutcomeStatus(JsonNode testGroupNode) {
        JsonNode jsonNode = testGroupNode.get("Outcome");
        String outcomeStatus;
        if (jsonNode != null) {
            outcomeStatus = String.valueOf(jsonNode.get("value")).replaceAll("\"", "");
        } else {
            outcomeStatus = String.valueOf(testGroupNode.get("ActionOutcome").get("value")).replaceAll("\"", "");
        }
        return outcomeStatus.equals("Passed")
                || outcomeStatus.equals("Done")
                || outcomeStatus.equals("Failed");
    }
}

