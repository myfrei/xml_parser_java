package ru.white.xml_parser_java.model;

public class TestResult {
    private String name;
    private String status;
    private String value;
    private String unitValue;
    private UnitOption unitOption;
    private String validValues;
    private boolean selected;

    @Override
    public String toString() {
        return "TestResult{" +
                "name='" + name + '\'' +
                ", status='" + status + '\'' +
                ", value='" + value + '\'' +
                ", unitValue='" + unitValue + '\'' +
                ", unitOption=" + unitOption +
                ", validValues='" + validValues + '\'' +
                ", selected=" + selected +
                '}';
    }

    public TestResult() {
        this.selected = true;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getUnitValue() {
        return unitValue;
    }

    public void setUnitValue(String unitValue) {
        this.unitValue = unitValue;
    }

    public UnitOption getUnitOption() {
        return unitOption;
    }

    public void setUnitOption(UnitOption unitOption) {
        this.unitOption = unitOption;
    }

    public String getValidValues() {
        return validValues;
    }

    public void setValidValues(String validValues) {
        this.validValues = validValues;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
