package ru.white.xml_parser_java.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import ru.white.xml_parser_java.model.FileData;
import ru.white.xml_parser_java.model.TestGroup;
import ru.white.xml_parser_java.util.GlobalVariables;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FileService {

    private final XmlMapper xmlMapper;
    private final TestGroupService testGroupService;

    public FileService(XmlMapper xmlMapper, TestGroupService testGroupService) {
        this.xmlMapper = xmlMapper;
        this.testGroupService = testGroupService;
    }

    // Возвращает список .xml файлов из директории по переданному пути.
    public List<File> getFilesByDirectory(String folderPath) {
        File folder = new File(folderPath);
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".xml"));
        return files != null ? List.of(files) : new ArrayList<>();
    }

    // Возвращает данные из .xml файла.
    public FileData getDataFromFile(File file) {
        FileData fileData = new FileData();
        try {
            JsonNode rootNode = xmlMapper.readTree(file);
            JsonNode resultSet = rootNode.path("TestResults").path("ResultSet");

            LocalDate date = parseDate(resultSet.path("startDateTime").asText());
            fileData.setDate(date);

            List<TestGroup> testGroups = GlobalVariables.TARGET_TESTS_TAG_NAMES.stream()
                    .map(tagName -> resultSet.path(tagName))
                    .filter(JsonNode::isContainerNode)
                    .flatMap(tagNode -> testGroupService.getTestGroupsByTagName(tagNode).stream())
                    .sorted()
                    .collect(Collectors.toList());

            fileData.setTestGroups(testGroups);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileData;
    }

    private LocalDate parseDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return null;
        }
        String[] dateParts = dateString.split("T")[0].split("-");
        return LocalDate.parse(String.join("-", dateParts), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }
}