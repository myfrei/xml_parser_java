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
    private ComboBox<UnitOption> comboBox;

    @FXML
    private Label valuePreview;

    @FXML
    public void initialize() {
        // Пробует распарсить значение в double, в случае неудачи отключает выпадающий список с вариантами единиц измерения.
        try {
            Double.parseDouble(testResult.getValue());
            valuePreview.setText(testResult.getUnitValue());
        } catch (Exception ex) {
            comboBox.setDisable(true);
            valuePreview.setText("");
        }

        // Инпуты и предпросмотр значения заполняются значениями из результата теста.
        treeViewInput.setText(testResult.getName());
        statusInput.setText(testResult.getStatus());
        valueInput.setText(testResult.getValue());
        validValuesInput.setText(testResult.getValidValues());
        // По нажатию 'cancelButton' окно закрывается, изменения пропадают.
        cancelButton.setOnAction(event -> {
            Stage stage = (Stage) cancelButton.getScene().getWindow();
            stage.close();
        });

        // Заполняет 'comboBox' вариантами значений из 'UnitOption'
        comboBox.setItems(FXCollections.observableArrayList(UnitOption.values()));
        comboBox.setValue(testResult.getUnitOption());

        // Обновляет значение предпросмотра 'unitValue' по выбору значения из выпадающего списка.
        comboBox.setOnAction(event -> {
            updateUnitValue();
        });

        // Обновляет значение предпросмотра 'unitValue' по вводу значения в 'valueInput'.
        valueInput.textProperty().addListener((ov, oldV, newV) -> {
            updateUnitValue();
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

            testResult.setUnitValue(valuePreview.getText());
            testResult.setUnitOption(comboBox.getValue());
            Label valueLabel = (Label) hBox.getChildren().get(2);
            valueLabel.setText(testResult.getUnitValue());

            testResult.setValidValues(validValuesInput.getText());
            Label validateValueLabel = (Label) hBox.getChildren().get(3);
            validateValueLabel.setText(validValuesInput.getText());
            Stage stage = (Stage) submitButton.getScene().getWindow();
            stage.close();
        });
    }

    // Обновляет значение предпросмотра 'unitValue' в зависимости от 'valueInput' и 'comboBox'
    private void updateUnitValue() {
        try {
            comboBox.setDisable(false);
            double doubleValue = Double.parseDouble(valueInput.getText()) * comboBox.getValue().getValue();
            if (comboBox.getValue().equals(UnitOption.Стандарт)) {
                valuePreview.setText(valueInput.getText());
            } else {
                valuePreview.setText(new DecimalFormat(getPattern()).format(doubleValue));
            }
        } catch (Exception e) {
            comboBox.setDisable(true);
            valuePreview.setText(valueInput.getText());
        }
    }

    // Возвращает подходящий паттерн для форматирования 'unitValue'
    private String getPattern() {
        StringBuilder stringItem = new StringBuilder();
        stringItem.append("#".repeat(Math.max(0, valueInput.getLength())));

        if (GlobalStates.getRoundingOptional().equals(RoundingOptionals.NO_ROUND)) {
            int maxAfterPointChars;
            if (comboBox.getValue().equals(UnitOption.Микро)) {
                maxAfterPointChars = valueInput.getText().split("\\.")[0].length() + 6;
            } else if (comboBox.getValue().equals(UnitOption.Милли)) {
                maxAfterPointChars = valueInput.getText().split("\\.")[0].length() + 3;
            } else {
                maxAfterPointChars = 2;
            }
            return stringItem + "." + "#".repeat(Math.max(0, maxAfterPointChars));
        } else if (GlobalStates.getRoundingOptional().equals(RoundingOptionals.TWO_UP)
                || GlobalStates.getRoundingOptional().equals(RoundingOptionals.TWO_DOWN)) {
            return stringItem + ".##";
        } else {
            return stringItem + ".###";
        }
    }
}