package ru.white.xml_parser_java.service;

import com.fasterxml.jackson.databind.JsonNode;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import ru.white.xml_parser_java.util.GlobalVariables;
import ru.white.xml_parser_java.util.StringManager;

import java.util.ArrayList;
import java.util.List;

public class GraphService {

    public GraphService() {
    }

    // Возвращает график, построенный по данным из узла или null, если график построить не удалось.
    public LineChart<Number, Number> getGraph(JsonNode testResultGroupNode) {
        try {
            JsonNode itemNode = testResultGroupNode.path("Data").path("Collection").path("Item");

            if (StringManager.removeQuotes(itemNode.path("name").asText()).equals(GlobalVariables.GRAPH_ITEM_NODE_NAME)) {
                JsonNode graphNode = itemNode.path("Collection").path("Item");

                double timeStep = parseTimeStep(graphNode);
                List<Double> points = parsePoints(graphNode);

                if (timeStep > 0 && !points.isEmpty()) {
                    return createLineChart(timeStep, points);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private double parseTimeStep(JsonNode graphNode) {
        return graphNode.path("Datum").path("value").asDouble(0.0);
    }

    private List<Double> parsePoints(JsonNode graphNode) {
        List<Double> points = new ArrayList<>();
        JsonNode elements = graphNode.path("IndexedArray").path("Element");

        elements.forEach(element -> points.add(element.path("value").asDouble()));
        return points;
    }

    // Создает график на основе временного шага и точек данных
    private LineChart<Number, Number> createLineChart(double timeStep, List<Double> points) {
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel(GlobalVariables.GRAPH_Y_LABEL);
        xAxis.setLabel(GlobalVariables.GRAPH_X_LABEL);

        LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle(GlobalVariables.GRAPH_TITLE);

        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        for (int i = 0; i < points.size(); i++) {
            series.getData().add(new XYChart.Data<>(timeStep * i, points.get(i)));
        }

        lineChart.setCreateSymbols(false);
        lineChart.setLegendVisible(false);
        lineChart.getData().add(series);

        return lineChart;
    }
}