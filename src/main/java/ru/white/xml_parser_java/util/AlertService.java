package ru.white.xml_parser_java.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ru.white.xml_parser_java.XMLViewerApplication;
import ru.white.xml_parser_java.controller.AlertController;

import java.io.IOException;

public class AlertService {
    // Открывает окно с сообщением/предупреждением
    public static void openAlertWindow(String message) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(XMLViewerApplication.class.getResource("alert.fxml"));
            AlertController alertController = new AlertController(message);
            fxmlLoader.setController(alertController);
            Scene scene = new Scene(fxmlLoader.load(), GlobalVariables.ALERT_WINDOW_SIZES[0], GlobalVariables.ALERT_WINDOW_SIZES[1]);
            Stage stage = new Stage();
            stage.setTitle(GlobalVariables.ALERT_WINDOW_TITLE);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

