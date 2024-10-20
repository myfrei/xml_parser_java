package ru.white.xml_parser_java.model;

import java.util.List;

public class TestGroup implements Comparable<TestGroup> {
    private String name;
    private String originName;
    private String status;
    private String stageType;

    public String getStageType() {
        return stageType;
    }

    public void setStageType(String stageType) {
        this.stageType = stageType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "TestGroup{" +
                "name='" + name + '\'' +
                ", status=" + status +
                ", stageType=" + stageType +
                ", tests=" + tests +
                '}';
    }

    private List<Test> tests;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOriginName() {
        return originName;
    }

    public void setOriginName(String originName) {
        this.originName = originName;
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
