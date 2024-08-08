package ru.white.xml_parser_java.service;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.alignment.HorizontalAlignment;
import com.lowagie.text.alignment.VerticalAlignment;
import com.lowagie.text.pdf.PdfWriter;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.LineChart;
import javafx.scene.image.WritableImage;
import ru.white.xml_parser_java.model.TestGroup;
import ru.white.xml_parser_java.model.TestResult;
import ru.white.xml_parser_java.model.TestResultGroup;
import ru.white.xml_parser_java.util.GlobalStates;
import ru.white.xml_parser_java.util.GlobalVariables;
import ru.white.xml_parser_java.util.AlertService;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


public class PdfExportService {

    private final AlertService alertService;

    public PdfExportService(AlertService alertService) {
        this.alertService = alertService;
    }

    // Создаёт PDF файл в переданной директории.
    public void exportToPDF(List<TestGroup> testGroups, LocalDate date, String folderPath) {
        String pdfFileName = folderPath + "\\" + GlobalVariables.getPdfFileName(date);

        try (Document document = new Document()) {
            PdfWriter.getInstance(document, new FileOutputStream(pdfFileName));
            document.open();
            addDocumentTitle(document, date);

            testGroups.forEach(testGroup -> {
                if (containsValidResults(testGroup)) {
                    addTestGroupToDocument(document, testGroup);
                }
            });

            alertService.openAlertWindow(GlobalVariables.getPdfCreateMessage(folderPath, pdfFileName));
        } catch (DocumentException | IOException e) {
            e.printStackTrace();
            alertService.openAlertWindow(GlobalVariables.INCORRECT_GRAPH_MESSAGE);
        }
    }

    private void addDocumentTitle(Document document, LocalDate date) throws DocumentException {
        Paragraph docTitle = new Paragraph();
        Font docTitleFont = new Font(Font.HELVETICA, 16, Font.BOLD);
        Font docTitleDateFont = new Font(Font.HELVETICA, 12, Font.BOLD);
        docTitle.setAlignment(Element.ALIGN_CENTER);
        docTitle.add(new Chunk(GlobalVariables.PDF_TITLE, docTitleFont));
        docTitle.add(new Chunk(GlobalVariables.getPdfTitleDate(date), docTitleDateFont));
        document.add(docTitle);
        document.add(new Paragraph("\n\n"));
    }

    private void addTestGroupToDocument(Document document, TestGroup testGroup) {
        try {
            Paragraph testName = new Paragraph();
            Font testTitleFont = new Font(Font.HELVETICA, 12, Font.BOLD);
            testName.setAlignment(Element.ALIGN_CENTER);
            testName.add(new Chunk(testGroup.getName(), testTitleFont));
            document.add(testName);
            document.add(new Paragraph("\n"));

            AtomicInteger tableNumber = new AtomicInteger();
            testGroup.getTests().forEach(test -> {
                if (test.getResultGroups().stream().anyMatch(TestResultGroup::isSelected)) {
                    tableNumber.incrementAndGet();
                    addTestToDocument(document, test, tableNumber.get());
                }
            });

        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    private void addTestToDocument(Document document, ru.white.xml_parser_java.model.Test test, int tableNumber) {
        try {
            Font tableTitleFont = new Font(Font.HELVETICA, 10, Font.NORMAL);
            Paragraph tableTitle = new Paragraph(GlobalVariables.getTableName(test.getName(), tableNumber, test.getName()), tableTitleFont);
            document.add(tableTitle);

            Table table = createTestResultTable(test);
            document.add(table);

            addGraphsToDocument(document, test, tableNumber);
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    private Table createTestResultTable(ru.white.xml_parser_java.model.Test test) throws DocumentException {
        Table table = new Table(4);
        table.setPadding(3);
        table.setWidth(100);
        table.setWidths(new int[]{55, 15, 15, 15});

        table.addCell(createCell(GlobalVariables.TREE_VIEW_COLUMN_NAME, true));
        table.addCell(createCell(GlobalVariables.VALUE_COLUMN_NAME, true));
        table.addCell(createCell(GlobalVariables.VALID_VALUES_COLUMN_NAME, true));
        table.addCell(createCell(GlobalVariables.STATUS_COLUMN_NAME, true));

        test.getResultGroups().forEach(resultGroup -> {
            if (!resultGroup.getName().equals(GlobalVariables.GRAPH_NODE_NAME)) {
                addResultGroupToTable(table, resultGroup);
            }
        });

        return table;
    }

    private void addResultGroupToTable(Table table, TestResultGroup resultGroup) {
        if (resultGroup.isSelected()) {
            resultGroup.getResults().forEach(result -> addTestResultToTable(table, resultGroup, result));
        }
    }

    private void addTestResultToTable(Table table, TestResultGroup resultGroup, TestResult result) {
        table.addCell(createCell(resultGroup.getName(), false));
        table.addCell(createCell(result.getUnitValue().isBlank() ? "-" : result.getValue(), false));
        table.addCell(createCell(result.getValidValues().equals(GlobalVariables.VALID_VALUES_UNDEFINED) ? "-" : result.getValidValues(), false));
        table.addCell(createCell(resultGroup.getStatus(), false));
    }

    private void addGraphsToDocument(Document document, ru.white.xml_parser_java.model.Test test, int tableNumber) {
        test.getResultGroups().stream()
                .filter(resultGroup -> resultGroup.getGraph() != null && GlobalStates.isIncludeGraphToPdf())
                .forEach(resultGroup -> {
                    try {
                        Image graphImage = createGraphImage(resultGroup, tableNumber);
                        document.add(graphImage);
                        document.newPage();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
    }

    private Image createGraphImage(TestResultGroup resultGroup, int tableNumber) throws IOException, BadElementException {
        LineChart<Number, Number> graphLineChart = resultGroup.getGraph();
        graphLineChart.setTitle(GlobalVariables.getGraphPdfTitle(tableNumber));

        Scene graphScene = new Scene(graphLineChart, GlobalVariables.GRAPH_WINDOW_SIZES[0], GlobalVariables.GRAPH_WINDOW_SIZES[1]);
        WritableImage writableGraphImage = graphScene.getRoot().snapshot(new SnapshotParameters(), null);
        BufferedImage bufferedGraphImage = SwingFXUtils.fromFXImage(writableGraphImage, null);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(bufferedGraphImage, "png", byteArrayOutputStream);

        Image pdfGraphImage = Image.getInstance(byteArrayOutputStream.toByteArray());
        pdfGraphImage.setAlignment(Element.ALIGN_CENTER);
        return pdfGraphImage;
    }

    private Cell createCell(String content, boolean isHeader) {
        Cell cell = new Cell(new Phrase(content, new Font(Font.HELVETICA, 10, isHeader ? Font.BOLD : Font.NORMAL)));
        cell.setVerticalAlignment(VerticalAlignment.CENTER);
        cell.setHorizontalAlignment(isHeader ? HorizontalAlignment.CENTER : HorizontalAlignment.LEFT);
        if (isHeader) {
            cell.setBackgroundColor(new Color(226, 230, 233));
        }
        return cell;
    }

    private boolean containsValidResults(TestGroup testGroup) {
        return testGroup.getTests().stream().anyMatch(test -> test.getResultGroups().size() > 0);
    }
}