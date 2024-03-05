package ru.white.xml_parser_java.model;

import java.util.List;

public class Test {
    private String name;
    private List<TestResultGroup> resultGroups;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<TestResultGroup> getResultGroups() {
        return resultGroups;
    }

    public void setResultGroups(List<TestResultGroup> resultGroups) {
        this.resultGroups = resultGroups;
    }
}
