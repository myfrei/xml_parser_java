package ru.white.xml_parser_java.model;

import java.util.List;

public class TestGroup implements Comparable<TestGroup> {
    private String name;
    private List<Test> tests;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Test> getTests() {
        return tests;
    }

    public void setTests(List<Test> tests) {
        this.tests = tests;
    }

    @Override
    public int compareTo(TestGroup o) {
        try {
            int firstNum = Integer.parseInt(name.replaceAll("[^0-9]", ""));
            int secondNum = Integer.parseInt(o.getName().replaceAll("[^0-9]", ""));
            return firstNum - secondNum;
        } catch (Exception ex) {
            return 1;
        }
    }
}
