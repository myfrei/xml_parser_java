package ru.white.xml_parser_java.service;

import ru.white.xml_parser_java.model.Test;
import ru.white.xml_parser_java.model.TestGroup;
import ru.white.xml_parser_java.model.TestResult;
import ru.white.xml_parser_java.model.TestResultGroup;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TexExportService {

    // Создает TEX файл в переданной директории.
    public void exportToTEX(List<TestGroup> testGroups, LocalDate date, String folderPath) {
        String fileName = "export_" + date.toString() + ".tex";
        String filePath = folderPath + "/" + fileName;
        //System.out.println(testGroups);

//        List<Test> tests = testGroups.get(0).getTests();
//        tests.stream().forEach(x -> {
//            System.out.println(x.getName());
//            System.out.println(x.getResultGroups());
//            System.out.println("______");
//        });
        try (FileWriter writer = new FileWriter(filePath)) {
            StringBuilder texContent = new StringBuilder();
            for (TestGroup testGroup : testGroups) {
                texContent.append(formatTestGroup(testGroup));
            }
            writer.write(texContent.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String formatTestGroup(TestGroup testGroup) {
        StringBuilder sb = new StringBuilder();
        sb.append("\\VAR{full_number} {").append(testGroup.getOriginName()).append("}:  \\underline{\\VAR{h.add_background_color(h.exist_result(step_status))|lower}}.\n\n");

        sb.append("%% if step_status == 'Passed' or step_status == 'Failed'\n\n");
        sb.append("\t%% set prec = 1\n\n");

        // Хранение всех уникальных переменных
        Set<String> passFailTestSet = new HashSet<>();
        Set<String> numericLimitSet = new HashSet<>();
        Set<String> actionValuesSet = new HashSet<>();
        Set<String> statementValuesSet = new HashSet<>();
        Set<String> waveformSet = new HashSet<>();

        for (Test test : testGroup.getTests()) {
            for (TestResultGroup resultGroup : test.getResultGroups()) {
                String baseName = formatName(resultGroup.getName());

                switch (resultGroup.getStageType().replaceAll("\"","")) {
                    case "PassFailTest":
                        passFailTestSet.add(baseName + "_exists");
                        break;
                    case "NumericLimitTest":
                    case "NI_MultipleNumericLimitTest":
                        for (TestResult result : resultGroup.getResults()) {
                            numericLimitSet.add(baseName + "_" + formatName(result.getName()) + "_limits");
                        }
                        break;
                    case "Action":
                        for (TestResult result : resultGroup.getResults()) {
                            actionValuesSet.add(baseName + "_" + formatName(result.getName()) + "_values");
                        }
                        break;
                    case "Statement":
                        for (TestResult result : resultGroup.getResults()) {
                            statementValuesSet.add(baseName + "_" + formatName(result.getName()) + "_values");
                        }
                        break;
                }

                if (resultGroup.getGraph() != null) {
                    waveformSet.add(baseName + "_waveform");
                }
            }

        }

        // Объявляем переменные как пустые списки
        declareVariables(sb, passFailTestSet);
        declareVariables(sb, numericLimitSet);
        declareVariables(sb, actionValuesSet);
        declareVariables(sb, statementValuesSet);
        declareVariables(sb, waveformSet);

        // Заполняем переменные данными
        appendTestStatus(sb, passFailTestSet, "PassFailTest");
        appendActionValues(sb, actionValuesSet);
        appendStatementValues(sb, statementValuesSet);
        appendNumericLimits(sb, numericLimitSet);
        appendWaveforms(sb, waveformSet);

        sb.append("\t%% print(decimate_graph_rs_transmite_waveform)\n");
        sb.append("%% endif\n");

        return sb.toString();
    }

    private void declareVariables(StringBuilder sb, Set<String> variables) {
        for (String var : variables) {
            sb.append("\t%% set ").append(var).append(" = []\n");
        }
    }

    private void appendTestStatus(StringBuilder sb, Set<String> passFailTestSet, String testType) {
        if (!passFailTestSet.isEmpty()) {
            sb.append("\t%% set substeps_").append(testType).append(" = e.get_substeps(step, '").append(testType).append("', 'Main')\n");
            for (String var : passFailTestSet) {
                sb.append("\t%% set _ = ").append(var).append(".extend(e.get_test_status_by_name(substeps_").append(testType).append(", '")
                        .append(extractTestName(var)).append("'))\n");
            }
        }
    }

    private void appendActionValues(StringBuilder sb, Set<String> actionValuesSet) {
        if (!actionValuesSet.isEmpty()) {
            sb.append("\t%% set substeps_Action = e.get_substeps(step, 'Action', 'Main')\n");
            for (String var : actionValuesSet) {
                sb.append("\t%% set _ = ").append(var).append(".extend(e.get_value_and_status_from_action(substeps_Action, '")
                        .append(extractTestName(var)).append("', '").append(extractResultName(var)).append("'))\n");
            }
        }
    }

    private void appendStatementValues(StringBuilder sb, Set<String> statementValuesSet) {
        if (!statementValuesSet.isEmpty()) {
            sb.append("\t%% set substeps_Statement = e.get_substeps(step, 'Statement', 'Main')\n");
            for (String var : statementValuesSet) {
                sb.append("\t%% set _ = ").append(var).append(".extend(e.get_value_and_status_from_statement(substeps_Statement, '")
                        .append(extractTestName(var)).append("', '").append(extractResultName(var)).append("'))\n");
            }
        }
    }

    private void appendNumericLimits(StringBuilder sb, Set<String> numericLimitSet) {
        if (!numericLimitSet.isEmpty()) {
            sb.append("\t%% set substeps_NumericLimitTest = e.get_substeps(step, 'NumericLimitTest', 'Main')\n");
            for (String var : numericLimitSet) {
                sb.append("\t%% set _ = ").append(var).append(".extend(e.get_numeric_limits(substeps_NumericLimitTest, '")
                        .append(extractTestName(var)).append("', '").append(extractResultName(var)).append("'))\n");
            }
        }
    }

    private void appendWaveforms(StringBuilder sb, Set<String> waveformSet) {
        for (String var : waveformSet) {
            sb.append("\t%% if e.get_analog_waveform(step, '").append(extractTestName(var)).append("') != [] \n");
            sb.append("\t\t%% set _ = ").append(var).append(".extend(e.get_analog_waveform(step, '").append(extractTestName(var)).append("'))\n");
            sb.append("\t%% endif\n");
        }
    }

    // Вспомогательные методы для работы с именами переменных
    private String extractTestName(String var) {
        return var.split("_")[0];
    }

    private String extractResultName(String var) {
        String[] parts = var.split("_");
        return parts[parts.length - 2] + " " + parts[parts.length - 1];
    }

    private String formatName(String name) {
        return name.toLowerCase().replaceAll("[^a-z0-9]", "_");
    }
}