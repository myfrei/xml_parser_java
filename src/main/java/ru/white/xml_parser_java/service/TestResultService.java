package ru.white.xml_parser_java.service;

import com.fasterxml.jackson.databind.JsonNode;
import ru.white.xml_parser_java.model.LimitComparator;
import ru.white.xml_parser_java.model.RoundingOptionals;
import ru.white.xml_parser_java.model.TestResult;
import ru.white.xml_parser_java.model.UnitOption;
import ru.white.xml_parser_java.util.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static ru.white.xml_parser_java.util.GlobalVariables.STANDART_DELIMER;

public class TestResultService {
    // Возвращает список результатов тестов.
    public List<TestResult> getTestResults(JsonNode testResultGroupNode) {
        List<TestResult> result = new ArrayList<>();
        JsonNode testResultNodes = testResultGroupNode.get("TestResult");
        if (testResultNodes != null) {
            for (JsonNode testResultNode : JsonNodeManager.separateUnitedNodes(testResultNodes)) {
                TestResult testResult = new TestResult();
                testResult.setName(StringManager.removeQuotes(String.valueOf(testResultNode.get("name"))));
                testResult.setStageType(StringManager.getStateType(testResultNode));
                testResult.setStatus(StatusType.fromString(StringManager.removeQuotes(JsonNodeManager.getStatus(testResultNode))).getRussianTranslation()); // Статус из родительского узла
                testResult.setValue(getValue(testResultNode));
                testResult.setUnitValue(testResult.getValue());
                testResult.setUnitOption(UnitOption.Стандарт);
                // Получает сначала двойной лимит значений потом, если он не определён пробует получить одиночный.
                String limits = getLimits(testResultNode);
                if (limits.equals(GlobalVariables.VALID_VALUES_UNDEFINED)) {
                    limits = getSingleLimit(testResultNode);
                }
                testResult.setValidValues(limits);
                result.add(testResult);
            }
        }
        return result;
    }
    // Возвращает значение результата теста, округлённое согласно глобальному состоянию 'roundingOptional'
    private String getValue(JsonNode testResultNode) {
        try {
            String stringValue = StringManager.removeQuotes(String.valueOf(testResultNode.get("TestData").get("Datum").get("value")));
            return getRoundedValue(stringValue);

        } catch (Exception ex) {
            return null;
        }
    }

    public List<TestResult> getTestResultsFromSessionAction(JsonNode testResultGroupNode) {
        List<TestResult> result = new ArrayList<>();
        JsonNode testResultNodes = testResultGroupNode.get("Data");
        if (testResultNodes != null) {
            String state = testResultGroupNode.get("Extension").get("TSStepProperties").get("StepType").asText();
            if (state.equals("Statement") || state.equals("Action")) {
                JsonNode collectionNode = testResultNodes.get("Collection");
                if (collectionNode != null) {
                    for (JsonNode itemNode : JsonNodeManager.separateUnitedNodes(collectionNode.get("Item"))) {
                        TestResult testResult = new TestResult();

                        testResult.setName(StringManager.removeQuotes(String.valueOf(itemNode.get("name"))));
                        testResult.setStageType(StringManager.getStateType(itemNode));
                        testResult.setStatus(StatusType.fromString(StringManager.removeQuotes(JsonNodeManager.getStatus(testResultGroupNode))).getRussianTranslation()); // Статус из родительского узла
                        testResult.setValue(getValueFromDatum(itemNode));
                        testResult.setUnitValue(testResult.getValue());
                        testResult.setUnitOption(UnitOption.Стандарт);
                        testResult.setValidValues(GlobalVariables.VALID_VALUES_UNDEFINED); // Лимиты не указаны в данном случае
                        result.add(testResult);

                    }
                }
            }
        }
        return result; // Возвращаем результат для Data узла
    }

    // Возвращает значение из узла Datum для структуры Data
    private String getValueFromDatum(JsonNode itemNode) {
        String stringValue = StringManager.removeQuotes(String.valueOf(itemNode.get("Datum").get("value")));
        return getRoundedValue(stringValue);
    }

    // Парсит строку, округляет согласно глобальному состоянию 'roundingOptional' и возвращает значение.
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
                        return String.valueOf(doubleValue);
                }
            } catch (Exception ex) {
                return value;
            }
        } else {
            return value;
        }
    }

    // Возвращает диапазон допустимых значений результата теста, если он указан.
    private String getLimits(JsonNode testResultNode) {
        try {
            ArrayList<String> limitItems = new ArrayList<>();
            JsonNode limitsNode = testResultNode.get("TestLimits").get("Limits").get("LimitPair").get("Limit");
            for (JsonNode limitItem : limitsNode) {
                limitItems.add(StringManager.removeQuotes(String.valueOf(limitItem.get("Datum").get("value"))));
            }
            return limitItems.get(0) + STANDART_DELIMER + limitItems.get(1);
        } catch (Exception ex) {
            return GlobalVariables.VALID_VALUES_UNDEFINED;
        }
    }


    // Возвращает одиночный предел результата теста если он указан.
    private String getSingleLimit(JsonNode testResultNode) {
        try {
            JsonNode limitsNode = testResultNode.get("TestLimits").get("Limits").get("SingleLimit");
            Optional<LimitComparator> optComparator = LimitComparator.getByStringValue(String.valueOf(limitsNode.get("comparator")));
            String viewComparator;
            if (optComparator.isPresent()) {
                viewComparator = optComparator.get().getViewValue();
            } else {
                viewComparator = "";
            }
            return viewComparator + " " + StringManager.removeQuotes(String.valueOf(limitsNode.get("Datum").get("value")));

        } catch (Exception ex) {
            return GlobalVariables.VALID_VALUES_UNDEFINED;
        }
    }
}

