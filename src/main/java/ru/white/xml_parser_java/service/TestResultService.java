package ru.white.xml_parser_java.service;

import com.fasterxml.jackson.databind.JsonNode;
import ru.white.xml_parser_java.model.LimitComparator;
import ru.white.xml_parser_java.model.RoundingOptionals;
import ru.white.xml_parser_java.model.TestResult;
import ru.white.xml_parser_java.model.UnitOption;
import ru.white.xml_parser_java.util.GlobalStates;
import ru.white.xml_parser_java.util.GlobalVariables;
import ru.white.xml_parser_java.util.JsonNodeManager;
import ru.white.xml_parser_java.util.StatusType;
import ru.white.xml_parser_java.util.StringManager;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


public class TestResultService {

    public TestResultService() {
        // Конструктор для внедрения зависимостей
    }

    // Возвращает список результатов тестов
    public List<TestResult> getTestResults(JsonNode testResultGroupNode) {
        JsonNode testResultNodes = testResultGroupNode.path("TestResult");
        return JsonNodeManager.separateUnitedNodes(testResultNodes).stream()
                .map(this::createTestResult)
                .collect(Collectors.toList());
    }

    // Возвращает список результатов тестов из узла SessionAction
    public List<TestResult> getTestResultsFromSessionAction(JsonNode testResultGroupNode) {
        List<TestResult> results = new ArrayList<>();
        JsonNode dataNode = testResultGroupNode.path("Data");
        if (dataNode != null) {
            String stepType = testResultGroupNode.path("Extension").path("TSStepProperties").path("StepType").asText();
            if ("Statement".equals(stepType) || "Action".equals(stepType)) {
                JsonNode collectionNode = dataNode.path("Collection").path("Item");
                JsonNodeManager.separateUnitedNodes(collectionNode).forEach(itemNode -> {
                    TestResult result = new TestResult();
                    result.setName(StringManager.removeQuotes(itemNode.path("name").asText()));
                    result.setStatus(StatusType.fromString(JsonNodeManager.getStatus(testResultGroupNode)).getRussianTranslation());
                    result.setValue(getValueFromDatum(itemNode));
                    result.setUnitValue(result.getValue());
                    result.setUnitOption(UnitOption.Стандарт);
                    result.setValidValues(GlobalVariables.VALID_VALUES_UNDEFINED);
                    results.add(result);
                });
            }
        }
        return results;
    }

    // Создает объект TestResult из JsonNode
    private TestResult createTestResult(JsonNode testResultNode) {
        TestResult testResult = new TestResult();
        testResult.setName(StringManager.removeQuotes(testResultNode.path("name").asText()));
        testResult.setStatus(StatusType.fromString(JsonNodeManager.getStatus(testResultNode)).getRussianTranslation());
        testResult.setValue(getValue(testResultNode));
        testResult.setUnitValue(testResult.getValue());
        testResult.setUnitOption(UnitOption.Стандарт);
        String limits = getLimits(testResultNode).orElseGet(() -> getSingleLimit(testResultNode).orElse(GlobalVariables.VALID_VALUES_UNDEFINED));
        testResult.setValidValues(limits);
        return testResult;
    }

    // Возвращает значение результата теста, округлённое согласно глобальному состоянию 'roundingOptional'
    private String getValue(JsonNode testResultNode) {
        String value = StringManager.removeQuotes(testResultNode.path("TestData").path("Datum").path("value").asText());
        return getRoundedValue(value);
    }

    // Возвращает значение из узла Datum для структуры Data
    private String getValueFromDatum(JsonNode itemNode) {
        String value = StringManager.removeQuotes(itemNode.path("Datum").path("value").asText());
        return getRoundedValue(value);
    }

    // Округляет значение в зависимости от глобального состояния
    private String getRoundedValue(String value) {
        if (!GlobalStates.getRoundingOptional().equals(RoundingOptionals.NO_ROUND)) {
            try {
                double doubleValue = Double.parseDouble(value);
                BigDecimal bigDecimal = new BigDecimal(doubleValue);
                switch (GlobalStates.getRoundingOptional()) {
                    case TWO_UP:
                        return String.valueOf(bigDecimal.setScale(2, BigDecimal.ROUND_UP));
                    case TWO_DOWN:
                        return String.valueOf(bigDecimal.setScale(2, BigDecimal.ROUND_DOWN));
                    case THREE_UP:
                        return String.valueOf(bigDecimal.setScale(3, BigDecimal.ROUND_UP));
                    case THREE_DOWN:
                        return String.valueOf(bigDecimal.setScale(3, BigDecimal.ROUND_DOWN));
                    default:
                        return value;
                }
            } catch (NumberFormatException e) {
                return value;
            }
        } else {
            return value;
        }
    }

    // Возвращает диапазон допустимых значений результата теста, если он указан
    private Optional<String> getLimits(JsonNode testResultNode) {
        try {
            List<String> limitItems = JsonNodeManager.separateUnitedNodes(testResultNode.path("TestLimits").path("Limits").path("LimitPair").path("Limit"))
                    .stream()
                    .map(limitItem -> StringManager.removeQuotes(limitItem.path("Datum").path("value").asText()))
                    .collect(Collectors.toList());
            if (limitItems.size() >= 2) {
                return Optional.of(limitItems.get(0) + GlobalVariables.STANDART_DELIMER + limitItems.get(1));
            }
        } catch (Exception e) {
            // Ignore exceptions and return empty Optional
        }
        return Optional.empty();
    }

    // Возвращает одиночный предел результата теста, если он указан
    private Optional<String> getSingleLimit(JsonNode testResultNode) {
        try {
            JsonNode singleLimitNode = testResultNode.path("TestLimits").path("Limits").path("SingleLimit");
            String comparator = LimitComparator.getByStringValue(singleLimitNode.path("comparator").asText())
                    .map(LimitComparator::getViewValue)
                    .orElse("");
            String value = StringManager.removeQuotes(singleLimitNode.path("Datum").path("value").asText());
            return Optional.of(comparator + " " + value);
        } catch (Exception e) {
            // Ignore exceptions and return empty Optional
        }
        return Optional.empty();
    }
}