package ru.white.xml_parser_java.controller;

import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import ru.white.xml_parser_java.XMLViewerApplication;
import ru.white.xml_parser_java.model.*;
import ru.white.xml_parser_java.service.PdfExportService;
import ru.white.xml_parser_java.util.AlertService;
import ru.white.xml_parser_java.util.GlobalVariables;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class TestResultController {
    private final FileData fileData;

    public TestResultController(FileData fileData) {
        this.fileData = fileData;
    }

    // Хранит идексы выбранных в настоящий момент вкладок.
    private final int[] tabIndexes = new int[2];
    // Хранит все столбцы таблицы для быстрого доступа к ним с целью изменения размера.
    private final List<TableColumn<String, String>> tableColumns = new ArrayList<>();
    // Хранит все элементы таблицы для быстрого доступа к ним с целью изменения размера.
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
    public void initialize() {
        // Заполняет таблицу колонками
        tableInitialize();
        // Назначает выбранными вкладки с нулевым индесом.
        tabIndexes[0] = 0;
        tabIndexes[1] = 0;
        // Делает "выбранным" главный чек бокс (Выбрать всё)
        generalCheckBox.setSelected(true);

        // Заполняет вкладки первого уровня (группы тестов)
        fileData.getTestGroups().forEach(tg -> {
            Tab tab = new Tab(tg.getName());
            testGroupsPane.getTabs().add(tab);
        });

        // Заполняет вкладки второго уровня (тесты)
        fileData.getTestGroups().get(0).getTests().forEach(t -> {
            Tab tab = new Tab(t.getName());
            testsPane.getTabs().add(tab);
        });

        // Заполняет таблицу данными из первого теста первой группы тестов (если такой тест есть)
        if (fileData.getTestGroups().get(0).getTests().size() > 0) updateTable(tabIndexes[0], tabIndexes[1]);

        // При выборе группы тестов заполняет вкладки второго уровня (тесты)
        testGroupsPane.getSelectionModel().selectedItemProperty().addListener(observable -> {
            tabIndexes[0] = testGroupsPane.getSelectionModel().getSelectedIndex();
            tabIndexes[1] = 0;
            updateTestsPane(tabIndexes[0]);
        });
        // При выборе теста, заполняет таблицу
        testsPane.getSelectionModel().selectedItemProperty().addListener(observable -> {
            tabIndexes[1] = testsPane.getSelectionModel().getSelectedIndex();
            updateTable(tabIndexes[0], tabIndexes[1]);
        });

        // Слушает главный чек бокс и по его изменению присваивает всем результатам тестов его статус (выбран/не выбран)
        generalCheckBox.setOnAction(actionEvent -> {
            fileData.getTestGroups().forEach(tg -> {
                tg.getTests().forEach(t -> {
                    t.getResultGroups().forEach(rg -> {
                        rg.setSelected(generalCheckBox.isSelected());
                        rg.getResults().forEach(r -> {
                            r.setSelected(generalCheckBox.isSelected());
                        });
                    });
                });
            });
            updateTable(tabIndexes[0], tabIndexes[1]);
        });

        // По нажатию кнопки 'exportSelectedTestPDFButton' экспортирует выбранный тест в PDF
        exportSelectedTestPDFButton.setOnAction(actionEvent -> {
            if (checkTestGroupListForPDF(List.of(fileData.getTestGroups().get(tabIndexes[0])))) {
                DirectoryChooser directoryChooser = new DirectoryChooser();
                File selectedFolder = directoryChooser.showDialog(((Node) actionEvent.getTarget()).getScene().getWindow());
                if (selectedFolder != null) {
                    PdfExportService pdfExportService = new PdfExportService();
                    pdfExportService.getPDF(List.of(fileData.getTestGroups().get(tabIndexes[0])), fileData.getDate(), selectedFolder.getAbsolutePath());
                    AlertService.openAlertWindow(GlobalVariables.PDF_CREATED_MESSAGE);
                } else {
                    AlertService.openAlertWindow(GlobalVariables.CHOOSE_DIRECTORY_ALERT_MESSAGE);
                }
            } else {
                AlertService.openAlertWindow(GlobalVariables.EMPTY_TEST_GROUPS_MESSAGE);
            }
        });

        // По нажатию кнопки 'exportAllPDFButton' экспортирует все тесты в PDF
        exportAllPDFButton.setOnAction(actionEvent -> {
            if (checkTestGroupListForPDF(fileData.getTestGroups())) {
                DirectoryChooser directoryChooser = new DirectoryChooser();
                File selectedFolder = directoryChooser.showDialog(((Node) actionEvent.getTarget()).getScene().getWindow());
                if (selectedFolder != null) {
                    PdfExportService pdfExportService = new PdfExportService();
                    pdfExportService.getPDF(fileData.getTestGroups(), fileData.getDate(), selectedFolder.getAbsolutePath());
                    AlertService.openAlertWindow(GlobalVariables.PDF_CREATED_MESSAGE);
                } else {
                    AlertService.openAlertWindow(GlobalVariables.CHOOSE_DIRECTORY_ALERT_MESSAGE);
                }
            } else {
                AlertService.openAlertWindow(GlobalVariables.EMPTY_TEST_GROUPS_MESSAGE);
            }
        });
        // Слушает изменения размера главного окна и вызывает метод который подгоняет размеры внутренних элементов.
        ChangeListener<Number> stageSizeListener = (observable, oldValue, newValue) -> {
            changeSize();
        };
        testResultWindow.widthProperty().addListener(stageSizeListener);
        testResultWindow.heightProperty().addListener(stageSizeListener);
    }
    // Обновляет вкладки тестов после выбора вкладки группы тестов.
    private void updateTestsPane(int index) {
        tableCells.clear();
        if (accordion.getPanes().size() > 0) accordion.getPanes().removeAll(accordion.getPanes());
        testsPane.getTabs().remove(0, testsPane.getTabs().size());
        fileData.getTestGroups().get(index).getTests().forEach(t -> {
            Tab tab = new Tab(t.getName());
            testsPane.getTabs().add(tab);
        });
    }
    // Обновляет содержимое таблицы, после выбора вкладки теста.
    private void updateTable(int testGroupIndex, int testIndex) {
        tableCells.clear();
        if (testIndex >= 0) {
            if (accordion.getPanes().size() > 0) accordion.getPanes().removeAll(accordion.getPanes());
            for (TestResultGroup rg : fileData.getTestGroups().get(testGroupIndex).getTests().get(testIndex).getResultGroups()) {
                TitledPane titledPane = new TitledPane();
                HBox hBox = getGraphic(rg);
                hBox.setOnMouseClicked((mouseEvent) -> {
                    if (mouseEvent.getClickCount() == 2) {
                        openEditResultGroupWindow(rg, hBox);
                    }
                });
                titledPane.setGraphic(hBox);
                if (rg.getResults().size() > 0) {
                    List<HBox> hBoxes = new ArrayList<>();
                    rg.getResults().forEach(res -> {
                        hBoxes.add(getGraphic(res));
                    });
                    ListView<HBox> listView = new ListView<>();
                    listView.setItems(FXCollections.observableArrayList(hBoxes));
                    titledPane.setContent(listView);
                } else {
                    titledPane.setCollapsible(false);
                }
                accordion.getPanes().add(titledPane);
            }
        }
    }
    // Создаёт столбцы таблицы
    private void tableInitialize() {
        TableColumn<String, String> treeViewColumn = new TableColumn<>("Tree View");
        treeViewColumn.setText("Tree View");
        treeViewColumn.setPrefWidth((testResultWindow.getWidth() / 4) - 10);
        tableColumns.add(treeViewColumn);
        table.getColumns().add(treeViewColumn);
        TableColumn<String, String> statusColumn = new TableColumn<>("Status");
        statusColumn.setPrefWidth((testResultWindow.getWidth() / 4) - 10);
        tableColumns.add(statusColumn);
        table.getColumns().add(statusColumn);
        TableColumn<String, String> valueColumn = new TableColumn<>("Value");
        valueColumn.setPrefWidth((testResultWindow.getWidth() / 4) - 10);
        tableColumns.add(valueColumn);
        table.getColumns().add(valueColumn);
        TableColumn<String, String> validValuesColumn = new TableColumn<>("Valid Values");
        validValuesColumn.setPrefWidth((testResultWindow.getWidth() / 4) - 10);
        tableColumns.add(validValuesColumn);
        table.getColumns().add(validValuesColumn);
    }
    // Базовый метод возвращающий строку таблицы
    private HBox getBaseGraphic(String name, String status, boolean hasChildren) {
        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER_LEFT);
        Label nameLabel = new Label(name);
        nameLabel.setPrefWidth(hasChildren ? (testResultWindow.getWidth() / 4) - 25 : (testResultWindow.getWidth() / 4) - 10);
        nameLabel.setAlignment(Pos.CENTER_LEFT);
        tableCells.put(nameLabel, hasChildren);
        hBox.getChildren().add(nameLabel);
        Label statusLabel = new Label(status);
        statusLabel.setPrefWidth((testResultWindow.getWidth() / 4) - 10);
        statusLabel.setAlignment(Pos.CENTER);
        tableCells.put(statusLabel, false);
        hBox.getChildren().add(statusLabel);
        return hBox;
    }
    // Возвращает элемент разбитый на ячейки для заполнения строки в таблице (перегрузка для группы результатов).
    private HBox getGraphic(TestResultGroup resultGroup) {
        HBox hBox = getBaseGraphic(resultGroup.getName(), resultGroup.getStatus(), resultGroup.getResults().size() > 0);
        // Вставляет пустую ячейку в столбец 'Value'
        Label emptyLabel1 = new Label();
        emptyLabel1.setPrefWidth((testResultWindow.getWidth() / 4) - 10);
        tableCells.put(emptyLabel1, false);
        hBox.getChildren().add(emptyLabel1);
        // Вставляет пустую ячейку в столбец 'Valid Values'
        // или, если у группы результатов есть график располагает в ячейке кнопку 'Смотреть график'
        Label emptyLabel2 = new Label();
        if (resultGroup.getGraph() != null) {
            Button graphButton = new Button(GlobalVariables.SHOW_GRAPH_BUTTON_TEXT);
            emptyLabel2.setGraphic(graphButton);
            graphButton.setOnAction(event -> {
                showGraph(resultGroup);
            });
        }
        emptyLabel2.setPrefWidth((testResultWindow.getWidth() / 4) - 10);
        tableCells.put(emptyLabel2, false);
        hBox.getChildren().add(emptyLabel2);
        CheckBox checkBox = new CheckBox();
        checkBox.setPrefWidth(20);
        checkBox.setSelected(resultGroup.isSelected());
        hBox.getChildren().add(checkBox);
        checkBox.setOnAction(actionEvent -> {
            resultGroup.setSelected(checkBox.isSelected());
            resultGroup.getResults().forEach(res -> res.setSelected(checkBox.isSelected()));
            updateTable(tabIndexes[0], tabIndexes[1]);
        });
        return hBox;
    }
    // Возвращает элемент разбитый на ячейки для заполнения строки в таблице (перегрузка для результата).
    private HBox getGraphic(TestResult result) {
        HBox hBox = getBaseGraphic(result.getName(), result.getStatus(), false);
        Label valueLabel = new Label(result.getUnitValue());
        valueLabel.setPrefWidth((testResultWindow.getWidth() / 4) - 10);
        valueLabel.setAlignment(Pos.CENTER);
        tableCells.put(valueLabel, false);
        hBox.getChildren().add(valueLabel);
        Label validValueLabel = new Label(result.getValidValues());
        validValueLabel.setPrefWidth((testResultWindow.getWidth() / 4) - 10);
        validValueLabel.setAlignment(Pos.CENTER);
        tableCells.put(validValueLabel, false);
        hBox.getChildren().add(validValueLabel);
        CheckBox checkBox = new CheckBox();
        checkBox.setPrefWidth(20);
        checkBox.setSelected(result.isSelected());
        hBox.getChildren().add(checkBox);
        checkBox.setOnAction(actionEvent -> {
            result.setSelected(checkBox.isSelected());
        });
        hBox.setOnMouseClicked((mouseEvent) -> {
            if (mouseEvent.getClickCount() == 2) {
                openEditResultWindow(result, hBox);
            }
        });
        return hBox;
    }
    // Открывает форму для редактирования группы результатов
    private void openEditResultGroupWindow(TestResultGroup resultGroup, HBox hBox) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(XMLViewerApplication.class.getResource("edit-result-group.fxml"));
            EditResultGroupController editResultGroupController = new EditResultGroupController(resultGroup, hBox);
            fxmlLoader.setController(editResultGroupController);
            Scene scene = new Scene(fxmlLoader.load(), GlobalVariables.EDIT_RESULT_GROUP_WINDOW_SIZES[0], GlobalVariables.EDIT_RESULT_GROUP_WINDOW_SIZES[1]);
            Stage stage = new Stage();
            stage.setTitle(GlobalVariables.EDIT_RESULT_GROUP_WINDOW_TITLE);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // Открывает форму для редактирования результата
    private void openEditResultWindow(TestResult result, HBox hBox) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(XMLViewerApplication.class.getResource("edit-result.fxml"));
            EditResultController editResultController = new EditResultController(result, hBox);
            fxmlLoader.setController(editResultController);
            Scene scene = new Scene(fxmlLoader.load(), GlobalVariables.EDIT_RESULT_WINDOW_SIZES[0], GlobalVariables.EDIT_RESULT_WINDOW_SIZES[1]);
            Stage stage = new Stage();
            stage.setTitle(GlobalVariables.EDIT_RESULT_WINDOW_TITLE);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // Перед экспортом в PDF проверяет тесты на наличие валидных результатов.
    private boolean checkTestGroupListForPDF(List<TestGroup> testGroups) {
        for (TestGroup tg : testGroups) {
            for (Test t : tg.getTests()) {
                if (t.getResultGroups().size() > 0) {
                    return true;
                }
            }
        }
        return false;
    }
    // Изменяет размеры внутренних элементов при изменении размера главного окна.
    private void changeSize() {
        testGroupsPane.setPrefWidth(testResultWindow.getWidth());
        testsPane.setPrefWidth(testResultWindow.getWidth());
        table.setPrefWidth(testResultWindow.getWidth());
        accordion.setPrefWidth(testResultWindow.getWidth());
        accordion.setPrefHeight(testResultWindow.getHeight() - 120);
        tableColumns.forEach(tableColumn -> {
            tableColumn.setPrefWidth((testResultWindow.getWidth() / 4) - 10);
        });
        for (Map.Entry<Label, Boolean> cellEntry : tableCells.entrySet()) {
            // Для строк с вложенными в них результатами (имеющими стрелку слева) отступ больше чем для других.
            if (cellEntry.getValue()) {
                cellEntry.getKey().setPrefWidth((testResultWindow.getWidth() / 4) - 25);
            } else {
                cellEntry.getKey().setPrefWidth((testResultWindow.getWidth() / 4) - 10);
            }
        }
        exportSelectedTestPDFButton.setLayoutY(testResultWindow.getHeight() - 30);
        exportAllPDFButton.setLayoutY(testResultWindow.getHeight() - 30);
        generalCheckBox.setLayoutY(testResultWindow.getHeight() - 25);
        generalCheckBox.setLayoutX(testResultWindow.getWidth() - 170);
    }

    // Открывает окно с графиком.
    private void showGraph(TestResultGroup resultGroup) {
        Stage stage = new Stage();
        stage.setTitle(resultGroup.getName());
        stage.setScene(resultGroup.getGraph());
        stage.setWidth(resultGroup.getGraph().getWidth() * GlobalVariables.GRAPH_WIN_SIZE_COEFFICIENT);
        stage.setHeight(resultGroup.getGraph().getHeight() * GlobalVariables.GRAPH_WIN_SIZE_COEFFICIENT);
        stage.show();
    }
}