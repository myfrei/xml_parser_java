package ru.white.xml_parser_java.service;

import ru.white.xml_parser_java.model.*;
import ru.white.xml_parser_java.util.AlertService;
import ru.white.xml_parser_java.util.GlobalStates;
import ru.white.xml_parser_java.util.GlobalVariables;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class TexExportService {

    // Создает TEX файл в переданной директории.
    public void exportToTEX(List<TestGroup> testGroups, LocalDate date, String folderPath) {
        String fileName = folderPath + "\\" + GlobalVariables.getTexFileName(date);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write(getTexHeader(date));
            testGroups.forEach(tg -> {
                if (checkTestGroupForEmpty(tg)) {
                    try {
                        writer.write(getTexTestGroup(tg));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            writer.write(getTexFooter());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Формирование заголовка TEX документа.
    private String getTexHeader(LocalDate date) {
        return "\\documentclass{article}\n" +
                "\\usepackage{graphicx}\n" +
                "\\usepackage{longtable}\n" +
                "\\usepackage{array}\n\n" +
                "\\title{" + GlobalVariables.PDF_TITLE + "}\n" +
                "\\date{" + GlobalVariables.getPdfTitleDate(date) + "}\n\n" +
                "\\begin{document}\n\n" +
                "\\maketitle\n\n";
    }

    // Формирование TEX для групп тестов.
    private String getTexTestGroup(TestGroup testGroup) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("\\section{").append(testGroup.getName()).append("}\n\n");

        List<Integer> tableNumbers = new ArrayList<>();
        AtomicInteger tableNumber = new AtomicInteger();

        testGroup.getTests().forEach(test -> {
            if (test.getResultGroups().stream().anyMatch(TestResultGroup::isSelected)) {
                tableNumbers.add(tableNumber.incrementAndGet());
                sb.append(getTexTest(test, tableNumber.get()));
            }
        });

        return sb.toString();
    }

    // Формирование TEX для отдельных тестов.
    private String getTexTest(Test test, int tableNumber) {
        StringBuilder sb = new StringBuilder();
        sb.append("\\subsection{").append(GlobalVariables.getTableName(test.getName(), tableNumber, test.getName())).append("}\n\n")
                .append("\\begin{longtable}{| p{0.55\\textwidth} | p{0.15\\textwidth} | p{0.15\\textwidth} | p{0.15\\textwidth} |}\n")
                .append("\\hline\n")
                .append(GlobalVariables.TREE_VIEW_COLUMN_NAME).append(" & ")
                .append(GlobalVariables.VALUE_COLUMN_NAME).append(" & ")
                .append(GlobalVariables.VALID_VALUES_COLUMN_NAME).append(" & ")
                .append(GlobalVariables.STATUS_COLUMN_NAME).append(" \\\\ \\hline\n");

        test.getResultGroups().forEach(resultGroup -> {
            if (!resultGroup.getName().equals(GlobalVariables.GRAPH_NODE_NAME)) {
                if (resultGroup.getResults().stream().anyMatch(TestResult::isSelected)) {
                    resultGroup.getResults().forEach(result -> {
                        if (result.isSelected()) {
                            sb.append(resultGroup.getName()).append(" & ")
                                    .append(result.getUnitValue().isBlank() ? "-" : result.getValue()).append(" & ")
                                    .append(result.getValidValues().equals(GlobalVariables.VALID_VALUES_UNDEFINED) ? "-" : result.getValidValues()).append(" & ")
                                    .append(resultGroup.getStatus()).append(" \\\\ \\hline\n");
                        }
                    });
                } else if (resultGroup.isSelected()) {
                    sb.append(resultGroup.getName()).append(" & ")
                            .append("-").append(" & ")
                            .append("-").append(" & ")
                            .append(resultGroup.getStatus()).append(" \\\\ \\hline\n");
                }
            }
        });

        sb.append("\\end{longtable}\n\n");

        // Добавление графиков, если они есть
        List<TestResultGroup> testResultGroupsWithGraphs = test.getResultGroups()
                .stream()
                .filter(trg -> trg.getGraph() != null && trg.isSelected())
                .collect(Collectors.toList());

        if (!testResultGroupsWithGraphs.isEmpty() && GlobalStates.isIncludeGraphToPdf()) {
            testResultGroupsWithGraphs.forEach(graphResultGroup -> {
                // Тут нужно добавить логику для сохранения графика в файл и вставки ссылки на него в TEX документ
                // Например: sb.append("\\includegraphics[width=\\textwidth]{graph_").append(tableNumber).append(".png}\n\n");
            });
        }

        return sb.toString();
    }

    // Формирование подвала TEX документа.
    private String getTexFooter() {
        return "\\end{document}\n";
    }

    // Проверяет группу тестов на наличие результатов внутри.
    private boolean checkTestGroupForEmpty(TestGroup testGroup) {
        for (Test t : testGroup.getTests()) {
            if (t.getResultGroups().size() > 0) return true;
        }
        return false;
    }
}