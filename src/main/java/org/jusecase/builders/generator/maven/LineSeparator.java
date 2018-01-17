package org.jusecase.builders.generator.maven;

public enum LineSeparator {
    System(java.lang.System.lineSeparator()),
    Windows("\r\n"),
    Unix("\n");

    public final String value;

    LineSeparator(String value) {
        this.value = value;
    }

    public static LineSeparator fromString(String string) {
        if (string == null) {
            return System;
        }
        return LineSeparator.valueOf(string);
    }
}
