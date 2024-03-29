package ru.white.xml_parser_java.service;

import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.*;
import com.lowagie.text.alignment.HorizontalAlignment;
import com.lowagie.text.alignment.VerticalAlignment;
import com.lowagie.text.pdf.PdfWriter;
import ru.white.xml_parser_java.model.*;
import ru.white.xml_parser_java.util.AlertService;
import ru.white.xml_parser_java.util.GlobalStates;
import ru.white.xml_parser_java.util.GlobalVariables;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.LineChart;
import javafx.scene.image.WritableImage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


public class PdfExportService {
    // Создаёт PDF файл в переданной директории.
    public void getPDF(List<TestGroup> testGroups, LocalDate date, String folderPath) {
        try (Document document = new Document()) {
            PdfWriter.getInstance(document, new FileOutputStream(folderPath + "\\" + GlobalVariables.getPdfFileName(date)));
            document.open();
            Paragraph docTitle = new Paragraph();
            Font docTitleFont = new Font(null, 16, Font.BOLD);
            Font docTitleDateFont = new Font(null, 12, Font.BOLD);
            docTitle.setAlignment(Element.ALIGN_CENTER);
            docTitle.add(new Chunk(GlobalVariables.PDF_TITLE, docTitleFont));
            docTitle.add(new Chunk(GlobalVariables.getPdfTitleDate(date), docTitleDateFont));
            document.add(docTitle);
            document.add(new Paragraph("\n\n"));
            testGroups.forEach(tg -> {
                if (checkTestGroupForEmpty(tg)) {
                    Paragraph testName = new Paragraph();
                    Font testTitleFont = new Font(null, 12, Font.BOLD);
                    testName.setAlignment(Element.ALIGN_CENTER);
                    testName.add(new Chunk(tg.getName(), testTitleFont));

                    document.add(testName);
                    document.add(new Paragraph("\n"));
                    List<Integer> tableNumbers = new ArrayList<>();
                    List<PdfElement> elements = new ArrayList<>();
                    AtomicInteger tableNumber = new AtomicInteger();
                    tg.getTests().forEach(t -> {
                        if (t.getResultGroups().stream().anyMatch(TestResultGroup::isSelected)) {
                            tableNumbers.add(tableNumber.incrementAndGet());
                            Font tableTitleFont = new Font(null, 10, Font.NORMAL);
                            Paragraph tableTitle = new Paragraph();
                            tableTitle.add(new Chunk(GlobalVariables.getTableName(testName.getChunks().get(0).toString(), tableNumber.get(), t.getName()), tableTitleFont));

                            Table table = getTable(t);
                            PdfElement tableElement = new PdfElement();
                            tableElement.setNumber(tableNumber.get());
                            tableElement.setTitle(tableTitle);
                            tableElement.setType(PdfElementType.TABLE);
                            tableElement.setTable(table);
                            elements.add(tableElement);

                            // Ищет группы результатов тестов содержащие графики и если надохит, добавляет график в PDF.
                            List<TestResultGroup> testResultGroupsWithGraphs = t.getResultGroups()
                                    .stream()
                                    .filter(trg -> trg.getGraph() != null)
                                    .collect(Collectors.toList());
                            if (!testResultGroupsWithGraphs.isEmpty() && GlobalStates.isIncludeGraphToPdf()) {
                                testResultGroupsWithGraphs.forEach(graphResultGroup -> {
                                    if (graphResultGroup.isSelected()) {
                                        try {
                                            Image pdfGraphImage = getGraphImage(graphResultGroup, tableNumber.get());
                                            PdfElement graphElement = new PdfElement();
                                            graphElement.setNumber(tableNumber.get());
                                            graphElement.setType(PdfElementType.GRAPH);
                                            graphElement.setGraph(pdfGraphImage);
                                            elements.add(graphElement);
                                        } catch (Exception ex) {
                                            AlertService.openAlertWindow(GlobalVariables.INCORRECT_GRAPH_MESSAGE);
                                        }
                                    }
                                });
                            }
                        }
                    });
                    // Проходит по всем полученным элементам и добавляет их в PDF документ.
                    // При этом если у таблицы есть график они выносятся на отдельную страницу, одиночные таблицы следуют одна за другой.
                    tableNumbers.forEach(tn -> {
                        List<PdfElement> elems = elements.stream().filter(el -> el.getNumber() == tn).collect(Collectors.toList());
                        if (elems.size() > 1) {
                            document.newPage();
                            putTableToDocument(elems.stream().filter(el -> el.getType().equals(PdfElementType.TABLE)).findFirst(), document);
                            putGraphToDocument(elems.stream().filter(el -> el.getType().equals(PdfElementType.GRAPH)).findFirst(), document);
                            document.newPage();
                        } else {
                            putTableToDocument(elems.stream().filter(el -> el.getType().equals(PdfElementType.TABLE)).findFirst(), document);
                        }
                    });
                }
            });
        } catch (DocumentException | IOException de) {
            System.err.println(de.getMessage());
        }
    }
    // Возвращает таблицу для PDF файла
    private Table getTable(Test test) {
        Table result = new Table(4);
        result.setPadding(3);
        result.setWidth(100);
        result.setWidths(new int[]{55, 15, 15, 15});
        result.addCell(getCell(GlobalVariables.TREE_VIEW_COLUMN_NAME, false, true));
        result.addCell(getCell(GlobalVariables.VALUE_COLUMN_NAME, true, true));
        result.addCell(getCell(GlobalVariables.VALID_VALUES_COLUMN_NAME, true, true));
        result.addCell(getCell(GlobalVariables.STATUS_COLUMN_NAME, true, true));

        test.getResultGroups().forEach(resultGroup -> {
            if (!resultGroup.getName().equals(GlobalVariables.GRAPH_NODE_NAME)) { // если не является графиком.
                if (resultGroup.getResults().stream().anyMatch(TestResult::isSelected)) {
                    resultGroup.getResults().forEach(res -> {
                        if (res.isSelected()) {
                            result.addCell(getCell(resultGroup.getName(), false, false));
                            result.addCell(getCell(res.getUnitValue().isBlank() ? "-" : res.getValue(), true, false));
                            result.addCell(getCell(res.getValidValues().equals(GlobalVariables.VALID_VALUES_UNDEFINED) ? "-" : res.getValidValues(), true, false));
                            result.addCell(getCell(resultGroup.getStatus(), true, false));
                        }
                    });
                } else if (resultGroup.isSelected()) {
                    result.addCell(getCell(resultGroup.getName(), false, false));
                    result.addCell(getCell("-", true, false));
                    result.addCell(getCell("-", true, false));
                    result.addCell(getCell(resultGroup.getStatus(), true, false));
                }
            }
        });
        return result;
    }

    // Возвращает картинку с графиком для вставки в PDF.
    private synchronized Image getGraphImage(TestResultGroup graphResultGroup, int tableNumber) throws IOException {
        LineChart<Number, Number> graphLineChart = graphResultGroup.getGraph();
        graphLineChart.setTitle(GlobalVariables.getGraphPdfTitle(tableNumber));
        Scene graphScene = new Scene(graphLineChart, GlobalVariables.GRAPH_WINDOW_SIZES[0], GlobalVariables.GRAPH_WINDOW_SIZES[1]);
        WritableImage writableGraphImage = graphScene.getRoot().snapshot(new SnapshotParameters(), null);
        BufferedImage bufferedGraphImage = SwingFXUtils.fromFXImage(writableGraphImage, new BufferedImage(GlobalVariables.GRAPH_WINDOW_SIZES[0], GlobalVariables.GRAPH_WINDOW_SIZES[1], BufferedImage.TYPE_INT_ARGB));
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(bufferedGraphImage, "png", byteArrayOutputStream);
        Image pdfGraphImage = Image.getInstance(byteArrayOutputStream.toByteArray());
        pdfGraphImage.setAlignment(Element.ALIGN_CENTER);
        return pdfGraphImage;
    }

    // Помещает таблицу в PDF документ.
    private void putTableToDocument(Optional<PdfElement> optTableElement, Document document) {
        if (optTableElement.isPresent()) {
            PdfElement tableElement = optTableElement.get();
            document.add(tableElement.getTitle());
            document.add(tableElement.getTable());
            document.add(new Paragraph("\n\n"));
        }
    }

    // Помещает график в PDF документ.
    private void putGraphToDocument(Optional<PdfElement> optGraphElement, Document document) {
        if (optGraphElement.isPresent()) {
            PdfElement graphElement = optGraphElement.get();
            document.add(graphElement.getGraph());
            document.add(new Paragraph("\n\n"));
        }
    }

    // Возвращает ячейку для PDF таблицы.
    private Cell getCell(String content, boolean center, boolean header) {
        Cell result = new Cell();
        Font cellFont = new Font(null, 10, Font.NORMAL);
        result.add(new Chunk(content, cellFont));
        result.setVerticalAlignment(VerticalAlignment.CENTER);
        if (center) {
            result.setHorizontalAlignment(HorizontalAlignment.CENTER);
        }
        if (header) {
            result.setBackgroundColor(new Color(226, 230, 233));
        }
        return result;
    }

    // Проверяет группу тестов на наличие результатов внутри.
    private boolean checkTestGroupForEmpty(TestGroup testGroup) {
        for (Test t : testGroup.getTests()) {
            if (t.getResultGroups().size() > 0) return true;
        }
        return false;
    }
}