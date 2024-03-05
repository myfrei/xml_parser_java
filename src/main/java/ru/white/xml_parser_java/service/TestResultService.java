package ru.white.xml_parser_java.service;

import com.fasterxml.jackson.databind.JsonNode;
import ru.white.xml_parser_java.model.RoundingOptionals;
import ru.white.xml_parser_java.model.TestResult;
import ru.white.xml_parser_java.util.GlobalStates;
import ru.white.xml_parser_java.util.GlobalVariables;
import ru.white.xml_parser_java.util.JsonNodeManager;
import ru.white.xml_parser_java.util.StringManager;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class TestResultService {
    // Возвращает список результатов тестов.
    public List<TestResult> getTestResults(JsonNode testResultGroupNode) {
        List<TestResult> result = new ArrayList<>();
        JsonNode testResultNodes = testResultGroupNode.get("TestResult");
        if (testResultNodes != null) {
            for (JsonNode testResultNode : JsonNodeManager.separateUnitedNodes(testResultNodes)) {
                TestResult testResult = new TestResult();
                testResult.setName(StringManager.removeQuotes(String.valueOf(testResultNode.get("name"))));
                testResult.setStatus(JsonNodeManager.getStatus(testResultNode));
                testResult.setValue(getValue(testResultNode));
                testResult.setValidValues(getLimits(testResultNode));
                result.add(testResult);
            }
        }
        return result;
    }

    // Возвращает значение результата теста, округлённое согласно глобальному состоянию 'roundingOptional'
    private String getValue(JsonNode testResultNode) {
        String stringValue = StringManager.removeQuotes(String.valueOf(testResultNode.get("TestData").get("Datum").get("value")));
        if (!GlobalStates.getRoundingOptional().equals(RoundingOptionals.NO_ROUND)) {
            try {
                double numValue = Double.parseDouble(stringValue);
                BigDecimal bigDecimal = new BigDecimal(numValue);
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
                        return stringValue;
                }
            } catch (Exception ex) {
                return stringValue;
            }
        } else {
            return stringValue;
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
            return limitItems.get(0) + " - " + limitItems.get(1);
        } catch (Exception ex) {
            return GlobalVariables.VALID_VALUES_UNDEFINED;
        }
    }
}

