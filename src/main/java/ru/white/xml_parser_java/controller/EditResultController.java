package ru.white.xml_parser_java.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import ru.white.xml_parser_java.model.TestResult;

public class EditResultController {
    private final TestResult testResult;
    private final HBox hBox;

    public EditResultController(TestResult testResult, HBox hBox) {
        this.testResult = testResult;
        this.hBox = hBox;
    }

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
    public void initialize() {
        // Инпуты заполняются значениями из результата теста.
        treeViewInput.setText(testResult.getName());
        statusInput.setText(testResult.getStatus());
        valueInput.setText(testResult.getValue());
        validValuesInput.setText(testResult.getValidValues());

        // По нажатию 'cancelButton' окно закрывается, изменения пропадают.
        cancelButton.setOnAction(event -> {
            Stage stage = (Stage) cancelButton.getScene().getWindow();
            stage.close();
        });

        // По нажатию 'submitButton' меняет значения как в результирующем списке (в данном случае это testResult),
        // так и в отображении (hBox). После этого окно редактирования закрывается.
        submitButton.setOnAction(actionEvent -> {
            testResult.setName(treeViewInput.getText());
            Label treeViewLabel = (Label) hBox.getChildren().get(0);
            treeViewLabel.setText(treeViewInput.getText());

            testResult.setStatus(statusInput.getText());
            Label statusLabel = (Label) hBox.getChildren().get(1);
            statusLabel.setText(statusInput.getText());

            testResult.setValue(valueInput.getText());
            Label valueLabel = (Label) hBox.getChildren().get(2);
            valueLabel.setText(valueInput.getText());

            testResult.setValidValues(validValuesInput.getText());
            Label validateValueLabel = (Label) hBox.getChildren().get(3);
            validateValueLabel.setText(validValuesInput.getText());

            Stage stage = (Stage) submitButton.getScene().getWindow();
            stage.close();
        });
    }
}
