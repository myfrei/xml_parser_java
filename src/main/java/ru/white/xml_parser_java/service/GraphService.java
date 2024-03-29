package ru.white.xml_parser_java.service;

import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import ru.white.xml_parser_java.util.GlobalVariables;

public class GraphService {
    public Scene getGraph() {
        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Число приседаний");
        xAxis.setLabel("Number of Month");
        //creating the chart
        final LineChart<Number, Number> lineChart =
                new LineChart<Number, Number>(xAxis, yAxis);

        lineChart.setTitle("Stock Monitoring, 2010");
        //defining a series
        XYChart.Series series = new XYChart.Series();
        series.setName("My portfolio");
        //populating the series with data
        series.getData().add(new XYChart.Data(0.1, 23));
        series.getData().add(new XYChart.Data(0.2, 14));
        series.getData().add(new XYChart.Data(0.3, 15));
        series.getData().add(new XYChart.Data(0.4, 24));
        series.getData().add(new XYChart.Data(0.5, 34.8));
        series.getData().add(new XYChart.Data(0.6, 36));
        series.getData().add(new XYChart.Data(0.7, 22));
        series.getData().add(new XYChart.Data(0.8, 45));
        series.getData().add(new XYChart.Data(0.9, 43));
        series.getData().add(new XYChart.Data(1.0, 17));
        series.getData().add(new XYChart.Data(1.1, 29));
        series.getData().add(new XYChart.Data(1.2, 25));
        lineChart.setCreateSymbols(false);
        lineChart.getStyleClass().add("thick-chart");
        lineChart.getData().add(series);

        Scene scene = new Scene(lineChart, GlobalVariables.GRAPH_WINDOW_SIZES[0], GlobalVariables.GRAPH_WINDOW_SIZES[1]);
        return scene;
    }
}