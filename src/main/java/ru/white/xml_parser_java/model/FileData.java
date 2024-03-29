package ru.white.xml_parser_java.model;

import java.time.LocalDate;
import java.util.List;

public class FileData {
    private LocalDate date;
    private List<TestGroup> testGroups;

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public List<TestGroup> getTestGroups() {
        return testGroups;
    }

    public void setTestGroups(List<TestGroup> testGroups) {
        this.testGroups = testGroups;
    }
}