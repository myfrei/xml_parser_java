package ru.white.xml_parser_java.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import ru.white.xml_parser_java.util.GlobalVariables;

public class InstructionController {

    @FXML
    private TextArea instructionTextArea;

    @FXML
    private Button closeButton;

    public InstructionController() {
    }

    @FXML
    public void initialize() {
        setupInstructionText();
        setupCloseButton();
    }

    private void setupInstructionText() {
        instructionTextArea.setText(GlobalVariables.INSTRUCTION_TEXT);
        instructionTextArea.setWrapText(true);
    }

    private void setupCloseButton() {
        closeButton.setOnAction(event -> closeWindow());
    }

    private void closeWindow() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}