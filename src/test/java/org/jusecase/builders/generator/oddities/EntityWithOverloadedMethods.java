package org.jusecase.builders.generator.oddities;

@SuppressWarnings("unused")
public class EntityWithOverloadedMethods {
    private String value;

    public void setValue(String value) {
        this.value = value;
    }

    public void setValue(int value) {
        this.value = "" + value;
    }

    public void setValue(float value) {
        this.value = "" + value;
    }

    public void setValue(double value) {
        this.value = "" + value;
    }
}
