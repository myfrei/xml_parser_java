package ru.white.xml_parser_java;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import ru.white.xml_parser_java.controller.InstructionController;
import ru.white.xml_parser_java.controller.TestResultController;
import ru.white.xml_parser_java.model.FileData;
import ru.white.xml_parser_java.model.RoundingOptionals;
import ru.white.xml_parser_java.model.TestGroup;
import ru.white.xml_parser_java.service.FileService;
import ru.white.xml_parser_java.util.AlertService;
import ru.white.xml_parser_java.util.GlobalStates;
import ru.white.xml_parser_java.util.GlobalVariables;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class XMLViewerMainController {
    private FileService fileService;
    private DirectoryChooser directoryChooser;
    private List<File> currentDirectoryFiles;

    @FXML
    private ImageView imageView;

    @FXML
    private TextField fileNameInput;

    @FXML
    private TextField folderPathInput;

    @FXML
    private Button changeFolderByPathButton;

    @FXML
    private ListView<String> rootDirectoryViewer;

    @FXML
    private Button chooseFolderButton;

    @FXML
    private CheckBox anotherDirectoriesFlag;

    @FXML
    private CheckBox skipUserDefinedFlag;

    @FXML
    private CheckBox skipEmptyResultsFlag;

    @FXML
    private ComboBox<String> roundingChooser;

    @FXML
    private Button instructionButton;

    @FXML
    private Button startButton;

    @FXML
    public void initialize() {
        // Инициирует FileService, DirectoryChooser и currentDirectoryFiles.
        fileService = new FileService();
        directoryChooser = new DirectoryChooser();
        currentDirectoryFiles = fileService.getFilesByDirectory(GlobalVariables.ROOT_DIRECTORY_PATH);
        // Заполняет окно для просмотра файлов списком файлов из корневой директории
        rootDirectoryViewer.setItems(getFileNamesList(currentDirectoryFiles));
        // Делает неактивной инпут пути к папке и кнопку "Выбрать папку".
        chooseFolderButton.setDisable(true);
        folderPathInput.setDisable(true);
        changeFolderByPathButton.setDisable(true);
        // Заполняет выпадающий список для выбора варианта округления результата.
        roundingChooser.setItems(FXCollections.observableArrayList(RoundingOptionals.getListOfRoundingOptions()));
        roundingChooser.setValue(RoundingOptionals.NO_ROUND.getTitle());
        // Помещает изображение в ImageView.
        imageView.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream(GlobalVariables.IMAGE_PATH))));

        // Слушает инпут и по вводу запроса осуществляет фильтрацию списка доступных файлов.
        fileNameInput.textProperty().addListener((ov, oldV, newV) -> {
            List<String> fileNamesListBasedOnQuery = getFileNamesList(currentDirectoryFiles)
                    .stream()
                    .filter(fileName -> fileName.toLowerCase().contains(newV.toLowerCase()))
                    .collect(Collectors.toList());
            rootDirectoryViewer.setItems(FXCollections.observableArrayList(fileNamesListBasedOnQuery));
        });
        // Осуществляет переход по указанному в 'folderPathInput' пути по нажатию Enter при активном инпуте.
        folderPathInput.setOnAction(event -> {
            if (event.getEventType().toString().equals("ACTION")) {
                changeFolderByPath();
            }
        });

        // Осуществляет переход по указанному в 'folderPathInput' пути по нажатию кнопи 'Перейти' рядом с инпутом.
        changeFolderByPathButton.setOnAction(event -> {
            changeFolderByPath();
        });
        // Слушает флаг "Разрешить выбор файлов из других папок" и делает активной/не активной соответствующие кнопку и инпут.
        anotherDirectoriesFlag.setOnAction(actionEvent -> {
            chooseFolderButton.setDisable(!anotherDirectoriesFlag.isSelected());
            folderPathInput.setDisable(!anotherDirectoriesFlag.isSelected());
            changeFolderByPathButton.setDisable(!anotherDirectoriesFlag.isSelected());
        });

        // Выбор папки с файлами.
        chooseFolderButton.setOnAction(event -> {
            File selectedFolder = directoryChooser.showDialog(((Node) event.getTarget()).getScene().getWindow());
            if (selectedFolder != null) {
                folderPathInput.setText(selectedFolder.getAbsolutePath());
                currentDirectoryFiles.clear();
                currentDirectoryFiles.addAll(fileService.getFilesByDirectory(selectedFolder.getAbsolutePath()));
                rootDirectoryViewer.setItems(getFileNamesList(currentDirectoryFiles));
            } else {
                AlertService.openAlertWindow(GlobalVariables.CHOOSE_DIRECTORY_ALERT_MESSAGE);
            }
        });

        // Слушает флаг "Skip UserDefined" и изменяет соответствующее глобальное состояние.
        skipUserDefinedFlag.setOnAction(actionEvent -> {
            GlobalStates.setUserDefined(!skipUserDefinedFlag.isSelected());
        });

        // Слушает флаг "Skip empty results" и изменяет соответствующее глобальное состояние.
        skipEmptyResultsFlag.setOnAction(actionEvent -> {
            GlobalStates.setShowEmptyResults(!skipEmptyResultsFlag.isSelected());
        });

        // Слушает выпадающий список с вариатами округления результата и по изменению изменяет соответствющее глобальное состояние.
        roundingChooser.setOnAction(actionEvent -> {
            GlobalStates.setRoundingOptional(RoundingOptionals.getByTitle(roundingChooser.getValue()));
        });

        // По нажатию кнопки 'Start' запускает получение тестов и открытие окна с этими тестами.
        startButton.setOnAction(event -> {
            try {
                Optional<File> selectedFile = currentDirectoryFiles
                        .stream()
                        .filter(f -> f.getName().equals(rootDirectoryViewer.getSelectionModel().getSelectedItem()))
                        .findAny();
                if (selectedFile.isPresent()) {
                    FileData fileData = fileService.getDataFromFile(selectedFile.get());
                    if (fileData.getTestGroups().size() > 0) {
                        openResultWindow(fileData, selectedFile.get().getName());
                    }
                } else {
                    AlertService.openAlertWindow(GlobalVariables.CHOOSE_FILE_ALERT_MESSAGE);
                }
            } catch (Exception ex) {
                AlertService.openAlertWindow(GlobalVariables.INCORRECT_FILE_DATA_MESSAGE);
            }
        });

        // По нажатию кнопки 'Инструкция' открывает окно с инструкцией.
        instructionButton.setOnAction(actionEvent -> {
            openInstructionWindow();
        });
    }

    // Открывает папку путь которой указан в 'folderPathInput'.
    private void changeFolderByPath() {
        File selectedFolder = new File(folderPathInput.getText());
        if (selectedFolder.exists()) {
            currentDirectoryFiles.clear();
            currentDirectoryFiles.addAll(fileService.getFilesByDirectory(folderPathInput.getText()));
            rootDirectoryViewer.setItems(getFileNamesList(currentDirectoryFiles));
        } else  {
            AlertService.openAlertWindow(GlobalVariables.getIncorrectFolderPathMessage(folderPathInput.getText()));
        }
    }

    // Возвращает список имён файлов для отображения в главном окне.
    private ObservableList<String> getFileNamesList(List<File> filesList) {
        return FXCollections.observableArrayList(filesList.stream().map(File::getName).collect(Collectors.toList()));
    }

    // Открывает окно с результатами тестов
    private void openResultWindow(FileData fileData, String fileName) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(XMLViewerApplication.class.getResource("test-results.fxml"));
            TestResultController testResultController = new TestResultController(fileData);
            fxmlLoader.setController(testResultController);
            Scene scene = new Scene(fxmlLoader.load(), GlobalVariables.RESULT_WINDOW_SIZES[0], GlobalVariables.RESULT_WINDOW_SIZES[1]);
            Stage stage = new Stage();
            stage.setTitle(fileName);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openInstructionWindow() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(XMLViewerApplication.class.getResource("instruction.fxml"));
            fxmlLoader.setController(new InstructionController());
            Scene scene = new Scene(fxmlLoader.load(), GlobalVariables.INSTRUCTION_WINDOW_SIZES[0], GlobalVariables.INSTRUCTION_WINDOW_SIZES[1]);
            Stage stage = new Stage();
            stage.setTitle(GlobalVariables.INSTRUCTION_WINDOW_TITLE);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

