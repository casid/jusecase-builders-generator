package org.jusecase.builders.generator.oddities;

@SuppressWarnings("unused")
public class EntityWithNestedEnum {

    private NestedEnum value;

    public NestedEnum getValue() {
        return value;
    }

    public void setValue(NestedEnum value) {
        this.value = value;
    }

    public enum NestedEnum {
        A, B, C, D
    }
}
