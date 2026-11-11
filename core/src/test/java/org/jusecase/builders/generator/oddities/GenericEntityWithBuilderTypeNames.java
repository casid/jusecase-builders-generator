package org.jusecase.builders.generator.oddities;

@SuppressWarnings("unused")
public class GenericEntityWithBuilderTypeNames<T extends Number, B extends Comparable<B>> {
    public void setFirst(T first) {
    }

    public void setSecond(B second) {
    }
}
