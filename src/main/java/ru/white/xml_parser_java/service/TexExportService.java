package ru.white.xml_parser_java.service;

import ru.white.xml_parser_java.model.Test;
import ru.white.xml_parser_java.model.TestGroup;
import ru.white.xml_parser_java.model.TestResult;
import ru.white.xml_parser_java.model.TestResultGroup;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public class TexExportService {

    // Создает TEX файл в переданной директории.
    public void exportToTEX(List<TestGroup> testGroups, LocalDate date, String folderPath) {
        String fileName = "export_" + date.toString() + ".tex";
        String filePath = folderPath + "/" + fileName;
        //System.out.println(testGroups);
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
        sb.append("\\VAR{full_number} {").append(testGroup.getName()).append("}:  \\underline{\\VAR{h.add_background_color(h.exist_result(step_status))|lower}}.\n\n");

        sb.append("%% if step_status == 'Passed' or step_status == 'Failed'\n\n");
        sb.append("\t%% set prec = 1\n\n");

        // Сеттеры для всех возможных значений
        for (Test test : testGroup.getTests()) {
            for (TestResultGroup resultGroup : test.getResultGroups()) {
                sb.append(formatSetters(resultGroup));
            }
        }

        sb.append("\n\t%% set substeps = e.get_substeps(step, 'SequenceCall', 'Main') \n");
        sb.append("\t%% for substep in substeps\n");
        sb.append("\t\t%% set substep_name = e.get_step_name(substep)\n");

        for (Test test : testGroup.getTests()) {
            for (TestResultGroup resultGroup : test.getResultGroups()) {
                sb.append(formatSubsteps(resultGroup, test.getName()));
            }
        }

        sb.append("\t%% endfor\n");
        sb.append("%% endif\n");

        return sb.toString();
    }

    private String formatSetters(TestResultGroup resultGroup) {
        StringBuilder sb = new StringBuilder();
        String baseName = resultGroup.getName().toLowerCase().replaceAll("[^a-z0-9]", "_");
        sb.append("\t%% set ").append(baseName).append("_exists = []\n");
        if (resultGroup.getResults().isEmpty()) {
            sb.append("\t%% set ").append(baseName).append("_limits = []\n");
        } else {
            for (TestResult result : resultGroup.getResults()) {
                sb.append("\t%% set ").append(baseName).append("_").append(result.getName().toLowerCase().replaceAll("[^a-z0-9]", "_")).append("_values = []\n");
            }
        }
        return sb.toString();
    }

    private String formatSubsteps(TestResultGroup resultGroup, String testName) {
        StringBuilder sb = new StringBuilder();
        String baseName = resultGroup.getName().toLowerCase().replaceAll("[^a-z0-9]", "_");

        if (resultGroup.getResults().isEmpty()) {
            sb.append("\t\t%% set substeps_PassFailTest = e.get_substeps(substep, 'PassFailTest', 'Main')\n");
            sb.append("\t\t%% set _ = ").append(baseName).append("_exists.extend(e.get_test_status_by_name(substeps_PassFailTest, '").append(resultGroup.getName()).append("'))\n");
        } else {
            sb.append("\t\t%% set substeps_Action = e.get_substeps(substep, 'Action', 'Main')\n");
            sb.append("\t\t%% set _ = ").append(baseName).append("_exists.extend(e.get_test_status_by_name(substeps_Action, '").append(resultGroup.getName()).append("'))\n");
            sb.append("\t\t%% set substeps_Statement = e.get_substeps(substep, 'Statement', 'Main')\n");
            for (TestResult result : resultGroup.getResults()) {
                sb.append("\t\t%% set _ = ").append(baseName).append("_").append(result.getName().toLowerCase().replaceAll("[^a-z0-9]", "_")).append("_values.extend(e.get_value_and_status_from_action(substeps_Statement, '").append(resultGroup.getName()).append("', '").append(result.getName()).append("'))\n");
            }
        }

        if (resultGroup.getGraph() != null) {
            sb.append("\t\t%% if e.get_analog_waveform(substep, '").append(resultGroup.getName()).append("') != [] \n");
            sb.append("\t\t\t%% set _ = ").append(baseName).append("_waveform.extend(e.get_analog_waveform(substep, '").append(resultGroup.getName()).append("'))\n");
            sb.append("\t\t%% endif\n");
        }

        return sb.toString();
    }
}