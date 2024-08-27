package ru.white.xml_parser_java.model;

import javafx.scene.Scene;
import javafx.scene.chart.LineChart;

import java.util.List;

public class TestResultGroup {
    private String name;
    private String status;
    private List<TestResult> results;
    private LineChart<Number, Number> graph;
    private boolean selected;
    private boolean empty;
    private String stageType;

    public String getStageType() {
        return stageType;
    }

    public void setStageType(String stageType) {
        this.stageType = stageType;
    }

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

    // Всегда возвращает копию графика т.к нельзя использовать один и тот же график для разных сцен, а это необходимо по логике программы.
    public LineChart<Number, Number> getGraph() {
        if (this.graph != null) {
            final LineChart<Number, Number> lineChart = new LineChart<>(this.graph.getXAxis(), this.graph.getYAxis());
            lineChart.setTitle(graph.getTitle());
            lineChart.setCreateSymbols(false);
            lineChart.setLegendVisible(false);
            lineChart.getData().addAll(this.graph.getData());
            return lineChart;
        } else {
            return null;
        }
    }

    public void setGraph(LineChart<Number, Number> graph) {
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

    @Override
    public String toString() {
        return "TestResultGroup{" +
                "name='" + name + '\'' +
                ", status='" + status + '\'' +
                ", results=" + results +
                ", graph=" + graph +
                ", selected=" + selected +
                ", stageType=" + stageType +
                ", empty=" + empty +
                '}';
    }

    public void setEmpty(boolean empty) {
        this.empty = empty;
    }
}
