package ru.white.xml_parser_java.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import ru.white.xml_parser_java.model.RoundingOptionals;
import ru.white.xml_parser_java.model.TestResult;
import ru.white.xml_parser_java.model.UnitOption;
import ru.white.xml_parser_java.util.GlobalStates;
import java.text.DecimalFormat;

public class EditResultController {

    private final TestResult testResult;
    private final HBox hBox;

    @FXML
    private TextField treeViewInput;
    @FXML
    private TextField statusInput;
    @FXML
    private TextField valueInput;
    @FXML
    private TextField validValuesInput;
    @FXML
    private Button cancelButton;
    @FXML
    private Button submitButton;
    @FXML
    private ComboBox<UnitOption> comboBox;
    @FXML
    private Label valuePreview;

    public EditResultController(TestResult testResult, HBox hBox) {
        this.testResult = testResult;
        this.hBox = hBox;
    }

    @FXML
    public void initialize() {
        populateInputs();
        setupEventHandlers();
    }

    private void populateInputs() {
        if (isValidDouble(testResult.getValue())) {
            valuePreview.setText(testResult.getUnitValue());
        } else {
            comboBox.setDisable(true);
            valuePreview.setText("");
        }

        treeViewInput.setText(testResult.getName());
        statusInput.setText(testResult.getStatus());
        valueInput.setText(testResult.getValue());
        validValuesInput.setText(testResult.getValidValues());
        comboBox.setItems(FXCollections.observableArrayList(UnitOption.values()));
        comboBox.setValue(testResult.getUnitOption());
    }

    private boolean isValidDouble(String value) {
        try {
            Double.parseDouble(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void setupEventHandlers() {
        cancelButton.setOnAction(event -> closeWindow());
        submitButton.setOnAction(event -> handleSubmit());
        comboBox.setOnAction(event -> updateUnitValue());
        valueInput.textProperty().addListener((ov, oldV, newV) -> updateUnitValue());
    }

    private void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    private void handleSubmit() {
        updateTestResult();
        updateUI();
        closeWindow();
    }

    private void updateTestResult() {
        testResult.setName(treeViewInput.getText());
        testResult.setStatus(statusInput.getText());
        testResult.setUnitValue(valuePreview.getText());
        testResult.setUnitOption(comboBox.getValue());
        testResult.setValidValues(validValuesInput.getText());
    }

    private void updateUI() {
        ((Label) hBox.getChildren().get(0)).setText(testResult.getName());
        ((Label) hBox.getChildren().get(1)).setText(testResult.getStatus());
        ((Label) hBox.getChildren().get(2)).setText(testResult.getUnitValue());
        ((Label) hBox.getChildren().get(3)).setText(testResult.getValidValues());
    }

    private void updateUnitValue() {
        try {
            double doubleValue = Double.parseDouble(valueInput.getText()) * comboBox.getValue().getValue();
            valuePreview.setText(formatValue(doubleValue));
            comboBox.setDisable(false);
        } catch (NumberFormatException e) {
            comboBox.setDisable(true);
            valuePreview.setText(valueInput.getText());
        }
    }

    private String formatValue(double value) {
        if (comboBox.getValue().equals(UnitOption.Стандарт)) {
            return valueInput.getText();
        } else {
            return new DecimalFormat(getPattern()).format(value);
        }
    }

    private String getPattern() {
        StringBuilder pattern = new StringBuilder("#".repeat(Math.max(0, valueInput.getLength())));

        if (GlobalStates.getRoundingOptional().equals(RoundingOptionals.NO_ROUND)) {
            int maxAfterPointChars = determineDecimalPlaces();
            return pattern.append(".").append("#".repeat(Math.max(0, maxAfterPointChars))).toString();
        } else if (GlobalStates.getRoundingOptional().equals(RoundingOptionals.TWO_UP)
                || GlobalStates.getRoundingOptional().equals(RoundingOptionals.TWO_DOWN)) {
            return pattern.append(".##").toString();
        } else {
            return pattern.append(".###").toString();
        }
    }

    private int determineDecimalPlaces() {
        if (comboBox.getValue().equals(UnitOption.Микро)) {
            return valueInput.getText().split("\\.")[0].length() + 6;
        } else if (comboBox.getValue().equals(UnitOption.Милли)) {
            return valueInput.getText().split("\\.")[0].length() + 3;
        } else {
            return 2;
        }
    }
}