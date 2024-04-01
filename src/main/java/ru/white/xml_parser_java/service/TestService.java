package ru.white.xml_parser_java.service;

import ru.white.xml_parser_java.model.Test;
import ru.white.xml_parser_java.model.TestResult;
import ru.white.xml_parser_java.model.TestResultGroup;
import ru.white.xml_parser_java.model.UnitOption;
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
                    JsonNode outcomeOrActionOutcome = getOutcomeOrActionOutcome(testNode);
                    singleGroup.setStatus(String.valueOf(outcomeOrActionOutcome.get("value")).replaceAll("\"", ""));
                    if (singleGroup.getStatus().equals("Done")) {
                        List<TestResult> newTestResultList = getData(testNode);
                        singleGroup.setResults(newTestResultList);
                    } else {
                        singleGroup.setResults(new ArrayList<>());
                    }
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
        JsonNode outcomeOrActionOutcome = getOutcomeOrActionOutcome(testGroupNode);
        String outcomeStatus = String.valueOf(outcomeOrActionOutcome.get("value")).replaceAll("\"", "");
        return outcomeStatus.equals("Passed")
                || outcomeStatus.equals("Done")
                || outcomeStatus.equals("Failed");
    }

    private JsonNode getOutcomeOrActionOutcome(JsonNode testGroupNode) {
        JsonNode outcome = testGroupNode.get("Outcome");
        if (outcome != null) {
            return outcome;
        }
        JsonNode outcome1 = testGroupNode.get("ActionOutcome");
        if (outcome1 != null) {
            return outcome1;
        }
        return null;
    }

    private List<TestResult> getData(JsonNode testGroupNode) {
        JsonNode data = testGroupNode.get("Data");
        if (data == null) {
            return new ArrayList<>();
        }
        List<TestResult> resultList = new ArrayList<>();
        JsonNode collection = data.get("Collection");
        JsonNode item = collection.get("Item");
        JsonNode datum = item.get("Datum");
        JsonNode value = datum.get("value");
        String values = String.valueOf(value).replaceAll("\"", "");
        TestResult testResult = new TestResult();
        testResult.setValue(values);
        testResult.setUnitValue(values);
        testResult.setSelected(true);
        testResult.setValidValues("None");
        testResult.setStatus("Done");
        testResult.setUnitOption(UnitOption.NONE);
        testResult.setName(String.valueOf(item.get("name")));
        resultList.add(testResult);
        return resultList;
    }
}

