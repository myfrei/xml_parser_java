package ru.white.xml_parser_java.service;

import com.lowagie.text.Font;
import com.lowagie.text.*;
import com.lowagie.text.alignment.HorizontalAlignment;
import com.lowagie.text.alignment.VerticalAlignment;
import com.lowagie.text.pdf.PdfWriter;
import ru.white.xml_parser_java.model.TestGroup;
import ru.white.xml_parser_java.model.Test;
import ru.white.xml_parser_java.model.TestResultGroup;
import ru.white.xml_parser_java.model.TestResult;
import ru.white.xml_parser_java.util.GlobalVariables;

import java.awt.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

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
                    tg.getTests().forEach(t -> {
                        if (t.getResultGroups().stream().anyMatch(TestResultGroup::isSelected)) {
                            Font tableTitleFont = new Font(null, 10, Font.NORMAL);
                            Paragraph tableTitle = new Paragraph();
                            tableTitle.add(new Chunk(GlobalVariables.getTableName(testName.getChunks().get(0).toString(), tg.getTests().indexOf(t) + 1, t.getName()), tableTitleFont));
                            document.add(tableTitle);
                            document.add(getTable(t));
                            document.add(new Paragraph("\n\n"));
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
        });
        return result;
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
            result.setBackgroundColor(Color.CYAN);
        }

        return result;
    }

    private boolean checkTestGroupForEmpty(TestGroup testGroup) {
        for (Test t : testGroup.getTests()) {
            if (t.getResultGroups().size() > 0) return true;
        }
        return false;
    }
}
