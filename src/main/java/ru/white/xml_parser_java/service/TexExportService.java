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
import java.util.stream.Collectors;

public class TexExportService {

    // Создает TEX файл в переданной директории
    public void exportToTEX(List<TestGroup> testGroups, LocalDate date, String folderPath) {
        String fileName = "export_" + date.toString() + ".tex";
        String filePath = folderPath + "/" + fileName;

        try (FileWriter writer = new FileWriter(filePath)) {
            StringBuilder texContent = new StringBuilder();
            testGroups.forEach(testGroup -> texContent.append(formatTestGroup(testGroup)));
            writer.write(texContent.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Форматирует группу тестов для TEX файла
    private String formatTestGroup(TestGroup testGroup) {
        StringBuilder sb = new StringBuilder();
        sb.append("\\VAR{full_number} {").append(testGroup.getOriginName()).append("}:  \\underline{\\VAR{h.add_background_color(h.exist_result(step_status))|lower}}.\n\n");

        Set<String> uniqueSetters = new HashSet<>();
        Set<String> passFailTestSet = new HashSet<>();
        Set<String> actionSet = new HashSet<>();
        Set<String> statementSet = new HashSet<>();

        testGroup.getTests().forEach(test -> {
            test.getResultGroups().forEach(resultGroup -> {
                uniqueSetters.addAll(formatSetters(resultGroup));
                collectPassFailTests(passFailTestSet, resultGroup);
            });
        });

        uniqueSetters.forEach(sb::append);

        sb.append("\n\t%% set substeps = e.get_substeps(step, 'SequenceCall', 'Main') \n");
        sb.append("\t%% for substep in substeps\n");
        sb.append("\t\t%% set substep_name = e.get_step_name(substep)\n");

        passFailTestSet.forEach(sb::append);

        testGroup.getTests().forEach(test -> {
            test.getResultGroups().forEach(resultGroup -> sb.append(formatSubsteps(resultGroup, test.getName(), actionSet, statementSet)));
        });

        sb.append("\t%% endfor\n");
        sb.append("%% endif\n");

        return sb.toString();
    }

    // Собирает PassFailTests для текущего теста
    private void collectPassFailTests(Set<String> passFailTestSet, TestResultGroup resultGroup) {
        if (resultGroup.getName().equals("Check TM") || resultGroup.getName().equals("Set Voltages")) {
            StringBuilder sb = new StringBuilder();
            sb.append("\t\t%% set substeps_PassFailTest = e.get_substeps(substep, 'PassFailTest', 'Main')\n");
            if (resultGroup.getName().equals("Check TM")) {
                sb.append("\t\t%% set _ = check_tm_exists.extend(e.get_test_status_by_name(substeps_PassFailTest, 'Check TM'))\n");
            } else if (resultGroup.getName().equals("Set Voltages")) {
                sb.append("\t\t%% set _ = set_voltages_exists.extend(e.get_test_status_by_name(substeps_PassFailTest, 'Set Voltages'))\n");
            }
            passFailTestSet.add(sb.toString());
        }
    }

    // Форматирует сеттеры для текущего результата теста
    private Set<String> formatSetters(TestResultGroup resultGroup) {
        Set<String> setters = new HashSet<>();
        String baseName = resultGroup.getName().toLowerCase().replaceAll("[^a-z0-9]", "_");
        setters.add("\t%% set " + baseName + "_exists = []\n");
        if (resultGroup.getResults().isEmpty()) {
            setters.add("\t%% set " + baseName + "_limits = []\n");
        } else {
            resultGroup.getResults().forEach(result ->
                    setters.add("\t%% set " + baseName + "_" + result.getName().toLowerCase().replaceAll("[^a-z0-9]", "_") + "_values = []\n")
            );
        }
        return setters;
    }

    // Форматирует подшаги для текущего результата теста
    private String formatSubsteps(TestResultGroup resultGroup, String testName, Set<String> actionSet, Set<String> statementSet) {
        StringBuilder sb = new StringBuilder();
        String baseName = resultGroup.getName().toLowerCase().replaceAll("[^a-z0-9]", "_");

        if (resultGroup.getResults().isEmpty()) {
            String passFailTest = "\t\t%% set substeps_PassFailTest = e.get_substeps(substep, 'PassFailTest', 'Main')\n" +
                    "\t\t%% set _ = " + baseName + "_exists.extend(e.get_test_status_by_name(substeps_PassFailTest, '" + resultGroup.getName() + "'))\n";
            if (!actionSet.contains(passFailTest)) {
                sb.append(passFailTest);
                actionSet.add(passFailTest);
            }
        } else {
            String actionStep = "\t\t%% set substeps_Action = e.get_substeps(substep, 'Action', 'Main')\n" +
                    "\t\t%% set _ = " + baseName + "_exists.extend(e.get_test_status_by_name(substeps_Action, '" + resultGroup.getName() + "'))\n";
            if (!actionSet.contains(actionStep)) {
                sb.append(actionStep);
                actionSet.add(actionStep);
            }

            String statementStep = "\t\t%% set substeps_Statement = e.get_substeps(substep, 'Statement', 'Main')\n";
            if (!statementSet.contains(statementStep)) {
                sb.append(statementStep);
                statementSet.add(statementStep);
            }
            resultGroup.getResults().forEach(result -> {
                String valueStep = "\t\t%% set _ = " + baseName + "_" + result.getName().toLowerCase().replaceAll("[^a-z0-9]", "_") + "_values.extend(e.get_value_and_status_from_action(substeps_Statement, '" + resultGroup.getName() + "', '" + result.getName() + "'))\n";
                sb.append(valueStep);
            });
        }

        if (resultGroup.getGraph() != null) {
            sb.append("\t\t%% if e.get_analog_waveform(substep, '").append(resultGroup.getName()).append("') != [] \n");
            sb.append("\t\t\t%% set _ = ").append(baseName).append("_waveform.extend(e.get_analog_waveform(substep, '").append(resultGroup.getName()).append("'))\n");
            sb.append("\t\t%% endif\n");
        }

        return sb.toString();
    }
}