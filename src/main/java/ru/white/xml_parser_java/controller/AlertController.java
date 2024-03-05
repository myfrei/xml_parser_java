package ru.white.xml_parser_java.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

public class AlertController {
    private final String message;

    public AlertController(String message) {
        this.message = message;
    }

    @FXML
    private Text text;

    @FXML
    private Button button;

    @FXML
    public void initialize() {
        // Отображает по центру переданное сообщение.
        text.setText(message);
        text.setTextAlignment(TextAlignment.CENTER);

        // По нажатию кнопки окно закрывается.
        button.setOnAction(event -> {
            Stage stage = (Stage) button.getScene().getWindow();
            stage.close();
        });
    }
}