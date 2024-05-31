package ru.white.xml_parser_java.model;

public enum UnitOption {
    Стандарт(1),
    Микро(1000000),
    Милли(1000),
    Кило(0.001),
    Мега(0.000001);

    private final double value;

    UnitOption(double value) {
        this.value = value;
    }

    public double getValue() {
        return value;
    }
}