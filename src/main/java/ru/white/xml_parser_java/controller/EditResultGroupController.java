package ru.white.xml_parser_java.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import ru.white.xml_parser_java.model.TestResultGroup;

public class EditResultGroupController {

    private final TestResultGroup testResultGroup;
    private final HBox hBox;

    @FXML
    private TextField nameInput;

    @FXML
    private TextField statusInput;

    @FXML
    private Button cancelButton;

    @FXML
    private Button submitButton;

    public EditResultGroupController(TestResultGroup testResultGroup, HBox hBox) {
        this.testResultGroup = testResultGroup;
        this.hBox = hBox;
    }

    @FXML
    public void initialize() {
        populateInputs();
        setupEventHandlers();
    }

    private void populateInputs() {
        nameInput.setText(testResultGroup.getName());
        statusInput.setText(testResultGroup.getStatus());
    }

    private void setupEventHandlers() {
        cancelButton.setOnAction(event -> closeWindow());
        submitButton.setOnAction(event -> handleSubmit());
    }

    private void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    private void handleSubmit() {
        updateTestResultGroup();
        updateUI();
        closeWindow();
    }

    private void updateTestResultGroup() {
        testResultGroup.setName(nameInput.getText());
        testResultGroup.setStatus(statusInput.getText());
    }

    private void updateUI() {
        ((Label) hBox.getChildren().get(0)).setText(testResultGroup.getName());
        ((Label) hBox.getChildren().get(1)).setText(testResultGroup.getStatus());
    }
}
