package ru.white.xml_parser_java.service;

import com.fasterxml.jackson.databind.JsonNode;
import ru.white.xml_parser_java.model.Test;
import ru.white.xml_parser_java.model.TestGroup;
import ru.white.xml_parser_java.model.TestResultGroup;
import ru.white.xml_parser_java.util.GlobalStates;
import ru.white.xml_parser_java.util.GlobalVariables;
import ru.white.xml_parser_java.util.JsonNodeManager;
import ru.white.xml_parser_java.util.StringManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TestGroupService {

    private final TestService testService;

    public TestGroupService(TestService testService) {
        this.testService = testService;
    }

    // Возвращает список групп тестов по тегу
    public List<TestGroup> getTestGroupsByTagName(JsonNode node) {
        List<TestGroup> testGroups = new ArrayList<>();
        JsonNodeManager.separateUnitedNodes(node).forEach(testGroupNode -> {
            getTestGroupByNode(testGroupNode).ifPresent(testGroup -> {
                if (shouldAddTestGroup(testGroup)) {
                    testGroups.add(testGroup);
                }
            });
        });
        return testGroups;
    }

    private Optional<TestGroup> getTestGroupByNode(JsonNode testGroupNode) {
        TestGroup testGroup = new TestGroup();
        try {
            if (isValidTestGroup(testGroupNode)) {
                setTestGroupName(testGroup, testGroupNode);

                List<Test> tests = GlobalVariables.TARGET_TESTS_TAG_NAMES.stream()
                        .map(tagName -> testGroupNode.path(tagName))
                        .filter(JsonNode::isContainerNode)
                        .flatMap(tagNode -> testService.getTestByTagName(tagNode).stream())
                        .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

                testGroup.setTests(tests);
                return Optional.of(testGroup);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return Optional.empty();
    }

    private void setTestGroupName(TestGroup testGroup, JsonNode testGroupNode) {
        String name = testGroupNode.path("callerName").asText();
        if (name == null || name.isEmpty()) {
            name = testGroupNode.path("name").asText();
        }
        testGroup.setOriginName(name);
        testGroup.setName(StringManager.getTestGroupName(name));
    }

    private boolean isValidTestGroup(JsonNode testGroupNode) {
        String outcomeStatus = testGroupNode.path("Outcome").path("value").asText();
        return "Passed".equals(outcomeStatus) || "Failed".equals(outcomeStatus) || "Done".equals(outcomeStatus)
                || (GlobalStates.isUserDefined() && "UserDefined".equals(outcomeStatus));
    }

    private boolean shouldAddTestGroup(TestGroup testGroup) {
        return GlobalStates.isShowEmptyResults() || !containsOnlyEmptyResults(testGroup);
    }

    private boolean containsOnlyEmptyResults(TestGroup testGroup) {
        return testGroup.getTests().stream()
                .flatMap(test -> test.getResultGroups().stream())
                .allMatch(TestResultGroup::isEmpty);
    }
}
