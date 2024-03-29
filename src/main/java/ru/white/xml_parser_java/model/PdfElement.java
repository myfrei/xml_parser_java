package ru.white.xml_parser_java.model;


import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Table;

public class PdfElement {
    private int number;
    private Paragraph title;
    private PdfElementType type;
    private Table table;
    private Image graph;

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public Paragraph getTitle() {
        return title;
    }

    public void setTitle(Paragraph title) {
        this.title = title;
    }

    public PdfElementType getType() {
        return type;
    }

    public void setType(PdfElementType type) {
        this.type = type;
    }

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public Image getGraph() {
        return graph;
    }

    public void setGraph(Image graph) {
        this.graph = graph;
    }
}