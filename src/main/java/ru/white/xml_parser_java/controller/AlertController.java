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
    private Text alertText;

    @FXML
    private Button closeButton;

    @FXML
    public void initialize() {
        setupAlertMessage();
        setupCloseButton();
    }

    private void setupAlertMessage() {
        alertText.setText(message);
        alertText.setTextAlignment(TextAlignment.CENTER);
    }

    private void setupCloseButton() {
        closeButton.setOnAction(event -> closeWindow());
    }

    private void closeWindow() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}