package ru.white.xml_parser_java.controller;

import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import ru.white.xml_parser_java.model.FileData;
import ru.white.xml_parser_java.model.TestGroup;
import ru.white.xml_parser_java.model.TestResult;
import ru.white.xml_parser_java.model.TestResultGroup;
import ru.white.xml_parser_java.service.PdfExportService;
import ru.white.xml_parser_java.service.TexExportService;
import ru.white.xml_parser_java.util.AlertService;
import ru.white.xml_parser_java.util.GlobalStates;
import ru.white.xml_parser_java.util.GlobalVariables;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestResultController {

    private final FileData fileData;
    private final int[] tabIndexes = new int[2];
    private final List<TableColumn<String, String>> tableColumns = new ArrayList<>();
    private final HashMap<Label, Boolean> tableCells = new HashMap<>();

    @FXML
    private Pane testResultWindow;
    @FXML
    private TabPane testGroupsPane;
    @FXML
    private TabPane testsPane;
    @FXML
    private TableView<String> table;
    @FXML
    private Accordion accordion;
    @FXML
    private CheckBox generalCheckBox;
    @FXML
    private Button exportSelectedTestPDFButton;
    @FXML
    private Button exportAllPDFButton;
    @FXML
    private Button exportTexButton;
    @FXML
    private CheckBox includeGraphCheckBox;
    public TestResultController(FileData fileData) {
        this.fileData = fileData;
    }

    @FXML
    public void initialize() {
        setupUI();
        setupEventHandlers();
    }

    private void setupUI() {
        tableInitialize();
        initializeTabs();
        includeGraphCheckBox.setSelected(GlobalStates.isIncludeGraphToPdf());
    }

    private void tableInitialize() {
        createTableColumn("Тип данных", testResultWindow.getWidth() / 4 - 10);
        createTableColumn("Статус", testResultWindow.getWidth() / 4 - 10);
        createTableColumn("Значение", testResultWindow.getWidth() / 4 - 10);
        createTableColumn("Диапазон значения", testResultWindow.getWidth() / 4 - 10);
    }

    private void createTableColumn(String title, double width) {
        TableColumn<String, String> column = new TableColumn<>(title);
        column.setPrefWidth(width);
        tableColumns.add(column);
        table.getColumns().add(column);
    }

    private void initializeTabs() {
        tabIndexes[0] = 0;
        tabIndexes[1] = 0;
        generalCheckBox.setSelected(true);

        fileData.getTestGroups().forEach(tg -> {
            testGroupsPane.getTabs().add(new Tab(tg.getName()));
        });

        if (!fileData.getTestGroups().isEmpty()) {
            fileData.getTestGroups().get(0).getTests().forEach(t -> {
                testsPane.getTabs().add(new Tab(t.getName()));
            });
        }

        if (!fileData.getTestGroups().isEmpty() && !fileData.getTestGroups().get(0).getTests().isEmpty()) {
            updateTable(tabIndexes[0], tabIndexes[1]);
        }
    }

    private void setupEventHandlers() {
        generalCheckBox.setOnAction(event -> toggleAllSelections(generalCheckBox.isSelected()));
        exportSelectedTestPDFButton.setOnAction(event -> exportSelectedTestToPDF());
        exportAllPDFButton.setOnAction(event -> exportAllTestsToPDF());
        exportTexButton.setOnAction(event -> exportAllTestsToTex());
        testGroupsPane.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> updateTestTabs());
        testsPane.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> updateTable(tabIndexes[0], testsPane.getSelectionModel().getSelectedIndex()));

        ChangeListener<Number> stageSizeListener = (observable, oldValue, newValue) -> adjustSizes();
        testResultWindow.widthProperty().addListener(stageSizeListener);
        testResultWindow.heightProperty().addListener(stageSizeListener);
    }

    private void toggleAllSelections(boolean selected) {
        fileData.getTestGroups().forEach(tg -> {
            tg.getTests().forEach(t -> {
                t.getResultGroups().forEach(rg -> {
                    rg.setSelected(selected);
                    rg.getResults().forEach(r -> r.setSelected(selected));
                });
            });
        });
        updateTable(tabIndexes[0], tabIndexes[1]);
    }

    private void exportSelectedTestToPDF() {
        if (checkTestGroupListForPDF(List.of(fileData.getTestGroups().get(tabIndexes[0])))) {
            exportToPDF(List.of(fileData.getTestGroups().get(tabIndexes[0])));
        } else {
            AlertService.openAlertWindow(GlobalVariables.EMPTY_TEST_GROUPS_MESSAGE);
        }
    }

    private void exportAllTestsToPDF() {
        if (checkTestGroupListForPDF(fileData.getTestGroups())) {
            exportToPDF(fileData.getTestGroups());
        } else {
            AlertService.openAlertWindow(GlobalVariables.EMPTY_TEST_GROUPS_MESSAGE);
        }
    }

    private void exportToPDF(List<TestGroup> testGroups) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedFolder = directoryChooser.showDialog(testResultWindow.getScene().getWindow());
        if (selectedFolder != null) {
            PdfExportService pdfExportService = new PdfExportService(new AlertService());
            pdfExportService.exportToPDF(testGroups, fileData.getDate(), selectedFolder.getAbsolutePath());
            AlertService.openAlertWindow(GlobalVariables.getPdfCreateMessage(selectedFolder.getAbsolutePath(), GlobalVariables.getPdfFileName(fileData.getDate())));
        } else {
            AlertService.openAlertWindow(GlobalVariables.CHOOSE_DIRECTORY_ALERT_MESSAGE);
        }
    }

    private void exportAllTestsToTex() {
        if (checkTestGroupListForPDF(fileData.getTestGroups())) {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            File selectedFolder = directoryChooser.showDialog(testResultWindow.getScene().getWindow());
            if (selectedFolder != null) {
                TexExportService texExportService = new TexExportService();
                texExportService.exportToTEX(fileData.getTestGroups(), fileData.getDate(), selectedFolder.getAbsolutePath());
                AlertService.openAlertWindow(GlobalVariables.getTexCreateMessage(selectedFolder.getAbsolutePath(), GlobalVariables.getTexFileName(fileData.getDate())));
            } else {
                AlertService.openAlertWindow(GlobalVariables.CHOOSE_DIRECTORY_ALERT_MESSAGE);
            }
        } else {
            AlertService.openAlertWindow(GlobalVariables.EMPTY_TEST_GROUPS_MESSAGE);
        }
    }

    private boolean checkTestGroupListForPDF(List<TestGroup> testGroups) {
        return testGroups.stream().flatMap(tg -> tg.getTests().stream()).anyMatch(t -> !t.getResultGroups().isEmpty());
    }

    private void updateTestTabs() {
        tabIndexes[0] = testGroupsPane.getSelectionModel().getSelectedIndex();
        tabIndexes[1] = 0;
        updateTestsPane(tabIndexes[0]);
    }

    private void updateTestsPane(int index) {
        tableCells.clear();
        accordion.getPanes().clear();
        testsPane.getTabs().clear();

        fileData.getTestGroups().get(index).getTests().forEach(t -> {
            testsPane.getTabs().add(new Tab(t.getName()));
        });
    }

    private void updateTable(int testGroupIndex, int testIndex) {
        tableCells.clear();
        accordion.getPanes().clear();
        if (testIndex >= 0) {
            fileData.getTestGroups().get(testGroupIndex).getTests().get(testIndex).getResultGroups().forEach(this::addResultGroupToAccordion);
        }
    }

    private void addResultGroupToAccordion(TestResultGroup rg) {
        TitledPane titledPane = new TitledPane();
        HBox hBox = createGroupRow(rg);
        titledPane.setGraphic(hBox);
        if (!rg.getResults().isEmpty()) {
            ListView<HBox> listView = createResultsListView(rg);
            titledPane.setContent(listView);
        } else {
            titledPane.setCollapsible(false);
        }
        accordion.getPanes().add(titledPane);
    }

    private HBox createGroupRow(TestResultGroup resultGroup) {
        HBox hBox = createBaseRow(resultGroup.getName(), resultGroup.getStatus(), !resultGroup.getResults().isEmpty());
        addPlaceholderCells(hBox);
        addGraphButtonIfNeeded(hBox, resultGroup);
        addCheckBox(hBox, resultGroup);
        return hBox;
    }

    private ListView<HBox> createResultsListView(TestResultGroup resultGroup) {
        List<HBox> resultRows = new ArrayList<>();
        resultGroup.getResults().forEach(res -> resultRows.add(createResultRow(res)));
        ListView<HBox> listView = new ListView<>(FXCollections.observableArrayList(resultRows));
        return listView;
    }

    private HBox createResultRow(TestResult result) {
        HBox hBox = createBaseRow(result.getName(), result.getStatus(), false);
        addValueAndRangeCells(hBox, result);
        addCheckBox(hBox, result);
        return hBox;
    }

    private HBox createBaseRow(String name, String status, boolean hasChildren) {
        HBox hBox = new HBox();
        Label nameLabel = createLabel(name, hasChildren ? testResultWindow.getWidth() / 4 - 25 : testResultWindow.getWidth() / 4 - 10);
        Label statusLabel = createLabel(status, testResultWindow.getWidth() / 4 - 10);
        hBox.getChildren().addAll(nameLabel, statusLabel);
        tableCells.put(nameLabel, hasChildren);
        tableCells.put(statusLabel, false);
        return hBox;
    }

    private Label createLabel(String text, double width) {
        Label label = new Label(text);
        label.setPrefWidth(width);
        label.setAlignment(Pos.CENTER_LEFT);
        return label;
    }

    private void addPlaceholderCells(HBox hBox) {
        Label placeholder1 = createLabel("", testResultWindow.getWidth() / 4 - 10);
        Label placeholder2 = createLabel("", testResultWindow.getWidth() / 4 - 10);
        tableCells.put(placeholder1, false);
        tableCells.put(placeholder2, false);
        hBox.getChildren().addAll(placeholder1, placeholder2);
    }

    private void addValueAndRangeCells(HBox hBox, TestResult result) {
        Label valueLabel = createLabel(result.getUnitValue(), testResultWindow.getWidth() / 4 - 10);
        Label validValueLabel = createLabel(result.getValidValues(), testResultWindow.getWidth() / 4 - 10);
        tableCells.put(valueLabel, false);
        tableCells.put(validValueLabel, false);
        hBox.getChildren().addAll(valueLabel, validValueLabel);
    }

    private void addGraphButtonIfNeeded(HBox hBox, TestResultGroup resultGroup) {
        if (resultGroup.getGraph() != null) {
            Button graphButton = new Button(GlobalVariables.SHOW_GRAPH_BUTTON_TEXT);
            graphButton.setOnAction(event -> showGraph(resultGroup));
            Label graphButtonContainer = new Label();
            graphButtonContainer.setGraphic(graphButton);
            graphButtonContainer.setPrefWidth(testResultWindow.getWidth() / 4 - 10);
            tableCells.put(graphButtonContainer, false);
            hBox.getChildren().add(graphButtonContainer);
        }
    }

    private void addCheckBox(HBox hBox, TestResultGroup resultGroup) {
        CheckBox checkBox = createCheckBox(resultGroup.isSelected());
        checkBox.setOnAction(event -> {
            resultGroup.setSelected(checkBox.isSelected());
            resultGroup.getResults().forEach(res -> res.setSelected(checkBox.isSelected()));
            updateTable(tabIndexes[0], tabIndexes[1]);
        });
        hBox.getChildren().add(checkBox);
    }

    private void addCheckBox(HBox hBox, TestResult result) {
        CheckBox checkBox = createCheckBox(result.isSelected());
        checkBox.setOnAction(event -> result.setSelected(checkBox.isSelected()));
        hBox.getChildren().add(checkBox);
    }

    private CheckBox createCheckBox(boolean selected) {
        CheckBox checkBox = new CheckBox();
        checkBox.setSelected(selected);
        checkBox.setPrefWidth(20);
        return checkBox;
    }

    private void showGraph(TestResultGroup resultGroup) {
        Stage stage = new Stage();
        stage.setTitle(resultGroup.getName());
        stage.setScene(new Scene(resultGroup.getGraph(), GlobalVariables.GRAPH_WINDOW_SIZES[0] * GlobalVariables.GRAPH_WIN_SIZE_COEFFICIENT, GlobalVariables.GRAPH_WINDOW_SIZES[1] * GlobalVariables.GRAPH_WIN_SIZE_COEFFICIENT));
        stage.show();
    }

    private void adjustSizes() {
        testGroupsPane.setPrefWidth(testResultWindow.getWidth());
        testsPane.setPrefWidth(testResultWindow.getWidth());
        table.setPrefWidth(testResultWindow.getWidth());
        accordion.setPrefWidth(testResultWindow.getWidth());
        accordion.setPrefHeight(testResultWindow.getHeight() - 120);

        tableColumns.forEach(tableColumn -> tableColumn.setPrefWidth(testResultWindow.getWidth() / 4 - 10));

        for (Map.Entry<Label, Boolean> cellEntry : tableCells.entrySet()) {
            cellEntry.getKey().setPrefWidth(cellEntry.getValue() ? testResultWindow.getWidth() / 4 - 25 : testResultWindow.getWidth() / 4 - 10);
        }

        exportSelectedTestPDFButton.setLayoutY(testResultWindow.getHeight() - 30);
        exportAllPDFButton.setLayoutY(testResultWindow.getHeight() - 30);
        exportTexButton.setLayoutY(testResultWindow.getHeight() - 30);
        generalCheckBox.setLayoutY(testResultWindow.getHeight() - 25);
        generalCheckBox.setLayoutX(testResultWindow.getWidth() - 180);
        includeGraphCheckBox.setLayoutY(testResultWindow.getHeight() - 25);
        includeGraphCheckBox.setLayoutX(testResultWindow.getWidth() - 350);
    }
}