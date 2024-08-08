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

    private final TestResultService testResultService;
    private final GraphService graphService;

    public TestResultGroupService(TestResultService testResultService, GraphService graphService) {
        this.testResultService = testResultService;
        this.graphService = graphService;
    }

    // Возвращает группы результатов теста
    public List<TestResultGroup> getTestResultGroups(JsonNode testNode) {
        List<TestResultGroup> testResultGroups = new ArrayList<>();
        GlobalVariables.TARGET_TESTS_TAG_NAMES.forEach(tagName -> {
            JsonNode testResultGroupsNode = testNode.path(tagName);
            if (testResultGroupsNode != null) {
                JsonNodeManager.separateUnitedNodes(testResultGroupsNode).forEach(testResultGroupNode -> {
                    TestResultGroup testResultGroup = createTestResultGroup(testResultGroupNode);
                    if (!testResultGroup.getResults().isEmpty() || testResultGroup.getGraph() != null) {
                        testResultGroups.add(testResultGroup);
                    }
                });
            }
        });
        return mergeSameNamesTestResultGroups(testResultGroups);
    }

    private TestResultGroup createTestResultGroup(JsonNode testResultGroupNode) {
        TestResultGroup testResultGroup = new TestResultGroup();
        setTestResultGroupName(testResultGroup, testResultGroupNode);
        testResultGroup.setStatus(StatusType.fromString(JsonNodeManager.getStatus(testResultGroupNode)).getRussianTranslation());
        testResultGroup.setSelected(true);

        List<TestResult> testResults = testResultService.getTestResults(testResultGroupNode);
        if (testResults.isEmpty()) {
            testResults = testResultService.getTestResultsFromSessionAction(testResultGroupNode);
        }
        testResultGroup.setResults(testResults);

        if (testResultGroup.getName().contains(GlobalVariables.GRAPH_NODE_NAME)) {
            testResultGroup.setGraph(graphService.getGraph(testResultGroupNode));
        }

        return testResultGroup;
    }

    private void setTestResultGroupName(TestResultGroup testResultGroup, JsonNode testResultGroupNode) {
        String name = StringManager.removeQuotes(testResultGroupNode.path("callerName").asText());
        if (name == null || name.isEmpty()) {
            name = StringManager.removeQuotes(testResultGroupNode.path("name").asText());
        }
        testResultGroup.setName(name);
    }

    // Объединяет группы результатов с одинаковыми именами
    public List<TestResultGroup> mergeSameNamesTestResultGroups(List<TestResultGroup> testResultGroups) {
        List<TestResultGroup> mergedGroups = new ArrayList<>();
        testResultGroups.forEach(testResultGroup -> {
            TestResultGroup existingGroup = mergedGroups.stream()
                    .filter(group -> group.getName().equals(testResultGroup.getName()))
                    .findFirst()
                    .orElse(null);
            if (existingGroup == null) {
                mergedGroups.add(testResultGroup);
            } else {
                existingGroup.getResults().addAll(testResultGroup.getResults());
            }
        });
        return mergedGroups;
    }
}