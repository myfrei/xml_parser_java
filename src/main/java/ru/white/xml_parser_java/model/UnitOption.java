package ru.white.xml_parser_java.model;

public enum UnitOption {
    NONE(1),
    MICRO(0.000001),
    MILLI(0.001),
    KILO(1000),
    MEGA(1000000);

    private final double value;

    UnitOption(double value) {
        this.value = value;
    }

    public double getValue() {
        return value;
    }
}