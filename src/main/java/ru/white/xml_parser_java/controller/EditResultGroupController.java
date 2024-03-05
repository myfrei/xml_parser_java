package ru.white.xml_parser_java.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import ru.white.xml_parser_java.model.TestResultGroup;

public class EditResultGroupController {

    private final TestResultGroup testResultGroup;
    private final HBox hBox;

    public EditResultGroupController(TestResultGroup testResultGroup, HBox hBox) {
        this.testResultGroup = testResultGroup;
        this.hBox = hBox;
    }

    @FXML
    private TextField nameInput;

    @FXML
    private TextField statusInput;

    @FXML
    private Button cancelButton;

    @FXML
    private Button submitButton;

    @FXML
    public void initialize() {
        // Инпуты заполняются значениями из группы результатов теста.
        nameInput.setText(testResultGroup.getName());
        statusInput.setText(testResultGroup.getStatus());

        // По нажатию 'cancelButton' окно закрывается, изменения пропадают.
        cancelButton.setOnAction(event -> {
            Stage stage = (Stage) cancelButton.getScene().getWindow();
            stage.close();
        });

        // По нажатию 'submitButton' меняет значения как в результирующем списке (в данном случае это testResult),
        // так и в отображении (hBox). После этого окно редактирования закрывается.
        submitButton.setOnAction(actionEvent -> {
            testResultGroup.setName(nameInput.getText());
            Label treeViewLabel = (Label) hBox.getChildren().get(0);
            treeViewLabel.setText(nameInput.getText());

            testResultGroup.setStatus(statusInput.getText());
            Label statusLabel = (Label) hBox.getChildren().get(1);
            statusLabel.setText(statusInput.getText());

            Stage stage = (Stage) submitButton.getScene().getWindow();
            stage.close();
        });
    }
}

