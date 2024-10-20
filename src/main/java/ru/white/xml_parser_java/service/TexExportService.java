package ru.white.xml_parser_java.service;

import ru.white.xml_parser_java.model.Test;
import ru.white.xml_parser_java.model.TestGroup;
import ru.white.xml_parser_java.model.TestResult;
import ru.white.xml_parser_java.model.TestResultGroup;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

public class TexExportService {

    // Создает TEX файл в переданной директории.
    public void exportToTEX(List<TestGroup> testGroups, LocalDate date, String folderPath) {
        String fileName = "export_" + date.toString() + ".tex";
        String filePath = folderPath + "/" + fileName;

        try (FileWriter writer = new FileWriter(filePath)) {
            StringBuilder texContent = new StringBuilder();
            for (TestGroup testGroup : testGroups) {
                texContent.append(formatTestGroup(testGroup));
            }
            writer.write(texContent.toString() + testGroups.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String formatTestGroup(TestGroup testGroup) {
        StringBuilder sb = new StringBuilder();
        Set<String>initializedVariables = new HashSet<>();
        // Предположим, что у TestGroup есть методы getNumber(), getName(), getStatus()
        String fullNumber = String.valueOf(testGroup.getOriginName());
        String stepStatus = testGroup.getStatus();

        // Формирование первой строки
        sb.append("\\VAR{full_number} {").append(fullNumber).append("}:  ")
                .append("\\underline{\\VAR{h.add_background_color(h.exist_result(")
                .append("step_status").append("|lower}}.\n\n");

        // Проверка статуса шага
        if ("Passed".equalsIgnoreCase(stepStatus) || "Failed".equalsIgnoreCase(stepStatus)) {
            sb.append("%% if step_status == 'Passed' or step_status == 'Failed'\n\n");

            sb.append("\t%% set prec = 1\n\n");

            // Проходим по всем тестам и результатам
            for (Test test : testGroup.getTests()) {
                for (TestResultGroup resultGroup : test.getResultGroups()) {
                    List<String> variableNames = generateVariableNames(test, resultGroup);

                    for (String variableName : variableNames) {
                        // Инициализируем список только если переменная еще не была инициализирована
                        if (!initializedVariables.contains(variableName)) {
                            sb.append("\t%% set ").append(variableName).append(" = []\n");
                            initializedVariables.add(variableName);
                        }

                        // Добавляем данные в переменную
                        sb.append("\t%% set _ = ").append(variableName).append(".extend([])\n");
                    }
                }
            }

            sb.append("\n");

            // Обработка графиков
            processGraphs(testGroup, sb, initializedVariables);

            sb.append("%% endif\n");
        }

        return sb.toString();
    }

    private List<String> generateVariableNames(Test test, TestResultGroup resultGroup) {
        String testName = sanitizeName(test.getName());

        String stageType = resultGroup.getStageType();

        List<String> variableNames = new ArrayList<>();

        if ("PassFailTest".equals(stageType) || "Statement".equals(stageType)) {
            // Суффикс "_exists"
            variableNames.add(testName + "_exists");
        } else if ("NumericLimitTest".equals(stageType) || "NI_MultipleNumericLimitTest".equals(stageType)) {
            // Суффикс "_limits", используем имена результатов
            if (resultGroup.getResults() != null && !resultGroup.getResults().isEmpty()) {
                for (TestResult result : resultGroup.getResults()) {
                    String resultName = sanitizeName(result.getName());
                    variableNames.add(testName + "_" + resultName + "_limits");
                }
            } else {
                variableNames.add(testName + "_limits");
            }
        } else if ("Action".equals(stageType)) {
            if (resultGroup.getGraph() != null) {
                // Суффикс "_waveform"
                variableNames.add(testName + "_waveform");
            } else {
                // Суффикс "_values"
                variableNames.add(testName + "_values");
            }
        } else {
            // По умолчанию используем суффикс "_values"
            variableNames.add(testName + "_values");
        }

        return variableNames;
    }

    private String sanitizeName(String name) {
        // Удаляем начальные и конечные пробелы, заменяем пробелы и дефисы на подчеркивания, приводим к нижнему регистру
        String sanitized = name.trim().toLowerCase().replaceAll("[-\\s]+", "_");
        // Убираем все символы, кроме букв, цифр и подчеркиваний
        sanitized = sanitized.replaceAll("[^a-z0-9_]", "");
        return sanitized;
    }

    private void processGraphs(TestGroup testGroup, StringBuilder sb, Set<String> initializedVariables) {
        // Проверяем наличие графиков и инициализируем переменные
        for (Test test : testGroup.getTests()) {
            for (TestResultGroup resultGroup : test.getResultGroups()) {
                if (resultGroup.getGraph() != null) {
                    String baseName = test.getName().trim().replaceAll("\\s+", "_").toLowerCase();
                    String variableName = baseName + "_waveform";

                    // Инициализируем переменную, если она еще не была инициализирована
                    if (!initializedVariables.contains(variableName)) {
                        sb.append("\t%% set ").append(variableName).append(" = []\n");
                        initializedVariables.add(variableName);
                    }

                    // Добавляем данные в переменную (пока пустой список)
                    sb.append("\t%% set _ = ").append(variableName).append(".extend([])\n");
                }
            }
        }
    }

}