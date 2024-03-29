package ru.white.xml_parser_java.service;

import com.fasterxml.jackson.databind.JsonNode;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import ru.white.xml_parser_java.util.GlobalVariables;
import ru.white.xml_parser_java.util.StringManager;

import java.util.ArrayList;
import java.util.List;

public class GraphService {
    // Возвращает сцену с графиком построенным по переданному узлу или null если график построить не удалось.
    public LineChart<Number, Number> getGraph(JsonNode testResultGroupNode) {
        try {
            // Ищет узел содержащий график
            JsonNode itemNode = testResultGroupNode.get("Data").get("Collection").get("Item");
            // Проверяет имя найденного узла
            if (StringManager.removeQuotes(String.valueOf(itemNode.get("name"))).equals(GlobalVariables.GRAPH_ITEM_NODE_NAME)) {
                // Доходит до внутреннего узла, который непосредственно содержит график.
                JsonNode graphNode = itemNode.get("Collection").get("Item");
                // Проходит по дочерним узлам и достаёт из них шаг по оси X (время) и массив значений по оси Y.
                double timeStep = 0.0; // для оси X
                List<Double> points = new ArrayList<>(); // для оси Y
                int index = 0;
                while (graphNode.has(index)) {
                    JsonNode innerNode = graphNode.get(index);
                    if (StringManager.removeQuotes(String.valueOf(innerNode.get("name"))).equals("dt")) {
                        timeStep = getTimeStep(innerNode);
                    } else if (StringManager.removeQuotes(String.valueOf(innerNode.get("name"))).equals("Y")) {
                        points.addAll(getPoints(innerNode));
                    }
                    index++;
                }
                // В случае если необходимые значения найдены возвращает график.
                if (timeStep > 0 && !points.isEmpty()) {
                    return getGraph(timeStep, points);
                }
            }
            // В случае ошибки, как и в случае если подходящие значения не найдены возвращает null.
        } catch (Exception ex) {
            return null;
        }
        return null;
    }

    // Возвращает шаг времени из узла (для оси X)
    private Double getTimeStep(JsonNode stepNode) {
        return Double.parseDouble(StringManager.removeQuotes(String.valueOf(stepNode.get("Datum").get("value"))));
    }

    // Возвращает массив значений для оси Y
    private List<Double> getPoints(JsonNode pointsNode) {
        List<Double> result = new ArrayList<>();
        JsonNode elementNode = pointsNode.get("IndexedArray").get("Element");
        int elementIndex = 0;
        while (elementNode.has(elementIndex)) {
            result.add(Double.parseDouble(StringManager.removeQuotes(String.valueOf(elementNode.get(elementIndex).get("value")))));
            elementIndex++;
        }
        return result;
    }

    // Возвращает сцену с построенным графиком.
    private LineChart<Number, Number> getGraph(double step, List<Double> points) {
        // Создаёт оси X и Y
        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel(GlobalVariables.GRAPH_Y_LABEL);
        xAxis.setLabel(GlobalVariables.GRAPH_X_LABEL);
        // Создаёт новый график с этими осями.
        final LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle(GlobalVariables.GRAPH_TITLE);
        // Определяет линию на графике.
        XYChart.Series series = new XYChart.Series();
        // Заполняет линию данными
        for (int i = 0; i < points.size(); i++) {
            series.getData().add(new XYChart.Data(step * i, points.get(i)));
        }
        // Настройки внешнего вида
        lineChart.setCreateSymbols(false); // Выключает точки на графике, оставляет только линию
        lineChart.setLegendVisible(false); // Выключает подсказки по цвету внизу графика, т.к. линия всего одна.
        // Добавляет линию в график
        lineChart.getData().add(series);
        return lineChart;
    }
}