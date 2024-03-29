package ru.white.xml_parser_java.model;

import javafx.scene.Scene;

import java.util.List;

public class TestResultGroup {
    private String name;
    private String status;
    private Scene graph;
    private List<TestResult> results;
    private boolean selected;
    private boolean empty;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Scene getGraph() {
        return graph;
    }

    public void setGraph(Scene graph) {
        this.graph = graph;
    }

    public List<TestResult> getResults() {
        return results;
    }

    public void setResults(List<TestResult> results) {
        this.results = results;
    }
    public boolean isSelected() {
        return selected;
    }
    public void setSelected(boolean selected) {
        this.selected = selected;
    }
    public boolean isEmpty() {
        return this.empty;
    }

    public void setEmpty(boolean empty) {
        this.empty = empty;
    }
}
