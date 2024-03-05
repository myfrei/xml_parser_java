module ru.white.xml_parser_java {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.databind;
    requires com.github.librepdf.openpdf;
    requires com.fasterxml.jackson.dataformat.xml;
    requires java.datatransfer;
    requires java.desktop;

    opens ru.white.xml_parser_java to javafx.fxml;
    opens ru.white.xml_parser_java.controller to javafx.fxml;
    exports ru.white.xml_parser_java;
}