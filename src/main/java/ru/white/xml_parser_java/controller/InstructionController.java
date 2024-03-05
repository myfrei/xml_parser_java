package ru.white.xml_parser_java.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import ru.white.xml_parser_java.util.GlobalVariables;

public class InstructionController {
    @FXML
    private TextArea textArea;

    @FXML
    private Button button;

    @FXML
    public void initialize() {
        // Заполняет textArea текстом инструкции.
        textArea.setText(GlobalVariables.INSTRUCTION_TEXT);
        textArea.setWrapText(true);

        // По нажатию кнопки окно закрывается.
        button.setOnAction(event -> {
            Stage stage = (Stage) button.getScene().getWindow();
            stage.close();
        });
    }
}
