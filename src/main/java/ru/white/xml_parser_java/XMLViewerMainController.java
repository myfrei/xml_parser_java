package ru.white.xml_parser_java;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
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
import ru.white.xml_parser_java.service.*;
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

    // Конструктор по умолчанию, необходимый для FXML
    public XMLViewerMainController() {
        this.fileService = new FileService(
                new XmlMapper(),
                new TestGroupService(
                        new TestService(
                                new TestResultGroupService(
                                        new TestResultService(), new GraphService()
                                )
                        )
                )
        );
        this.directoryChooser = new DirectoryChooser();
        this.currentDirectoryFiles = fileService.getFilesByDirectory(GlobalVariables.ROOT_DIRECTORY_PATH);
    }

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
        setupUIComponents();
        setupListeners();
        loadInitialDirectoryFiles();
    }

    private void setupUIComponents() {
        rootDirectoryViewer.setItems(getFileNamesList(currentDirectoryFiles));
        chooseFolderButton.setDisable(true);
        folderPathInput.setDisable(true);
        changeFolderByPathButton.setDisable(true);
        roundingChooser.setItems(FXCollections.observableArrayList(RoundingOptionals.getListOfRoundingOptions()));
        roundingChooser.setValue(RoundingOptionals.NO_ROUND.getTitle());
        imageView.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream(GlobalVariables.IMAGE_PATH))));
    }

    private void setupListeners() {
        fileNameInput.textProperty().addListener((ov, oldV, newV) -> filterFilesByQuery(newV));
        folderPathInput.setOnAction(event -> changeFolderByPath());
        changeFolderByPathButton.setOnAction(event -> changeFolderByPath());
        anotherDirectoriesFlag.setOnAction(event -> toggleDirectorySelection(anotherDirectoriesFlag.isSelected()));
        chooseFolderButton.setOnAction(event -> openDirectoryChooser());
        skipUserDefinedFlag.setOnAction(event -> GlobalStates.setUserDefined(!skipUserDefinedFlag.isSelected()));
        skipEmptyResultsFlag.setOnAction(event -> GlobalStates.setShowEmptyResults(!skipEmptyResultsFlag.isSelected()));
        roundingChooser.setOnAction(event -> GlobalStates.setRoundingOptional(RoundingOptionals.getByTitle(roundingChooser.getValue())));
        startButton.setOnAction(event -> startFileProcessing());
        instructionButton.setOnAction(event -> openInstructionWindow());
    }

    private void loadInitialDirectoryFiles() {
        rootDirectoryViewer.setItems(getFileNamesList(currentDirectoryFiles));
    }

    private void filterFilesByQuery(String query) {
        List<String> filteredFiles = currentDirectoryFiles.stream()
                .map(File::getName)
                .filter(fileName -> fileName.toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());
        rootDirectoryViewer.setItems(FXCollections.observableArrayList(filteredFiles));
    }

    private void toggleDirectorySelection(boolean enabled) {
        chooseFolderButton.setDisable(!enabled);
        folderPathInput.setDisable(!enabled);
        changeFolderByPathButton.setDisable(!enabled);
    }

    private void openDirectoryChooser() {
        File selectedFolder = directoryChooser.showDialog(getCurrentStage());
        if (selectedFolder != null) {
            updateDirectoryFiles(selectedFolder.getAbsolutePath());
        } else {
            AlertService.openAlertWindow(GlobalVariables.CHOOSE_DIRECTORY_ALERT_MESSAGE);
        }
    }

    private void changeFolderByPath() {
        File selectedFolder = new File(folderPathInput.getText());
        if (selectedFolder.exists()) {
            updateDirectoryFiles(folderPathInput.getText());
        } else {
            AlertService.openAlertWindow(GlobalVariables.getIncorrectFolderPathMessage(folderPathInput.getText()));
        }
    }

    private void updateDirectoryFiles(String folderPath) {
        currentDirectoryFiles = fileService.getFilesByDirectory(folderPath);
        rootDirectoryViewer.setItems(getFileNamesList(currentDirectoryFiles));
    }

    private ObservableList<String> getFileNamesList(List<File> filesList) {
        return FXCollections.observableArrayList(filesList.stream().map(File::getName).collect(Collectors.toList()));
    }

    private void startFileProcessing() {
        Optional<File> selectedFile = currentDirectoryFiles.stream()
                .filter(f -> f.getName().equals(rootDirectoryViewer.getSelectionModel().getSelectedItem()))
                .findFirst();
        if (selectedFile.isPresent()) {
            processSelectedFile(selectedFile.get());
        } else {
            AlertService.openAlertWindow(GlobalVariables.CHOOSE_FILE_ALERT_MESSAGE);
        }
    }

    private void processSelectedFile(File selectedFile) {
        try {
            FileData fileData = fileService.getDataFromFile(selectedFile);
            if (!fileData.getTestGroups().isEmpty()) {
                openResultWindow(fileData, selectedFile.getName());
            }
        } catch (Exception ex) {
            AlertService.openAlertWindow(GlobalVariables.INCORRECT_FILE_DATA_MESSAGE);
        }
    }

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

    private Stage getCurrentStage() {
        return (Stage) rootDirectoryViewer.getScene().getWindow();
    }
}