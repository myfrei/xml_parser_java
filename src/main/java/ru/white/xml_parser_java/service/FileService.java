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

    // Возвращает список .xml файлов из директории по переданному пути.
    public List<File> getFilesByDirectory(String folderPath) {
        List<File> result = new ArrayList<>();
        File folder = new File(folderPath);
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.getName().contains(".xml")) {
                    result.add(file);
                }
            }
        }
        return result;
    }

    // Возвращает данные из .xml файла.
    public FileData getDataFromFile(File file) {
        FileData result = new FileData();
        List<TestGroup> testGroups = new ArrayList<>();
        XmlMapper mapper = new XmlMapper();

        try {
            JsonNode resultSet = mapper.readTree(file).get("TestResults").get("ResultSet");
            // Получает дату теста.
            String dateString = String.valueOf(resultSet.get("startDateTime")).replaceAll("\"", "").split("T")[0];
            LocalDate date = LocalDate.parse(dateString, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            result.setDate(date);
            // Получает результаты тестов.
            GlobalVariables.TARGET_TESTS_TAG_NAMES.forEach(tagName -> {
                JsonNode currentTagNode = resultSet.get(tagName);
                if (currentTagNode != null) {
                    TestGroupService testGroupService = new TestGroupService();
                    List<TestGroup> tests = testGroupService.getTestGroupsByTagName(currentTagNode);
                    if (!tests.isEmpty()) {
                        testGroups.addAll(tests);
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        result.setTestGroups(testGroups.stream().sorted().collect(Collectors.toList()));
        return result;
    }
}

