package ru.white.xml_parser_java.service;

import com.fasterxml.jackson.databind.JsonNode;
import ru.white.xml_parser_java.model.Test;
import ru.white.xml_parser_java.model.TestResultGroup;
import ru.white.xml_parser_java.util.GlobalVariables;
import ru.white.xml_parser_java.util.JsonNodeManager;
import ru.white.xml_parser_java.util.StatusType;
import ru.white.xml_parser_java.util.StringManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TestService {

    private final TestResultGroupService testResultGroupService;

    public TestService(TestResultGroupService testResultGroupService) {
        this.testResultGroupService = testResultGroupService;
    }

    // Возвращает список тестов по тегу
    public List<Test> getTestByTagName(JsonNode node) {
        List<Test> tests = new ArrayList<>();
        JsonNodeManager.separateUnitedNodes(node).forEach(testNode -> {
            getTestByNode(testNode).ifPresent(tests::add);
        });
        return tests;
    }

    // Возвращает тест на основе JsonNode
    private Optional<Test> getTestByNode(JsonNode testNode) {
        if (isValidTest(testNode)) {
            Test test = new Test();
            setTestName(test, testNode);

            List<TestResultGroup> resultGroups = testResultGroupService.getTestResultGroups(testNode);
            if (resultGroups.isEmpty()) {
                addEmptyTestResultGroup(test, testNode);
            } else {
                test.setResultGroups(resultGroups);
            }
            return Optional.of(test);
        }
        return Optional.empty();
    }

    // Устанавливает имя теста
    private void setTestName(Test test, JsonNode testNode) {
        String name = StringManager.removeQuotes(testNode.path("callerName").asText());
        if (name.isEmpty()) {
            name = StringManager.removeQuotes(testNode.path("name").asText());
        }
        test.setName(name);
    }

    // Добавляет пустую группу результатов теста
    private void addEmptyTestResultGroup(Test test, JsonNode testNode) {
        TestResultGroup emptyGroup = new TestResultGroup();
        emptyGroup.setName(test.getName());
        emptyGroup.setStatus(StatusType.fromString(JsonNodeManager.getStatus(testNode)).getRussianTranslation());
        emptyGroup.setResults(testResultGroupService.getTestResultGroups(testNode).getFirst().getResults());
        emptyGroup.setSelected(true);
        emptyGroup.setEmpty(emptyGroup.getResults().isEmpty());

        if (emptyGroup.getName().contains(GlobalVariables.GRAPH_NODE_NAME)) {
            emptyGroup.setGraph(testResultGroupService.getTestResultGroups(testNode).getFirst().getGraph());
        }

        test.setResultGroups(List.of(emptyGroup));
    }

    // Проверяет валидность теста
    private boolean isValidTest(JsonNode testNode) {
        String outcomeStatus = testNode.path("Outcome").path("value").asText();
        if (outcomeStatus.isEmpty()) {
            outcomeStatus = testNode.path("ActionOutcome").path("value").asText();
        }
        return outcomeStatus.equals("Passed") || outcomeStatus.equals("Done") || outcomeStatus.equals("Failed");
    }
}