package ru.white.xml_parser_java;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ru.white.xml_parser_java.util.GlobalVariables;

import java.io.IOException;

public class XMLViewerApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(XMLViewerApplication.class.getResource("app.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), GlobalVariables.MAIN_WINDOW_SIZES[0], GlobalVariables.MAIN_WINDOW_SIZES[1]);
        stage.setTitle(GlobalVariables.MAIN_WINDOW_TITLE);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}