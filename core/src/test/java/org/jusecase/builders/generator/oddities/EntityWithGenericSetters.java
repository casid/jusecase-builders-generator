package org.jusecase.builders.generator.oddities;

import java.util.List;

@SuppressWarnings("unused")
public class EntityWithGenericSetters {
    public <X> void setItems(List<X> items) {
    }

    public <M extends Number & Comparable<M>> void setMulti(M multi) {
    }

    public <B> void setOther(B other) {
    }

    public <S extends Comparable<S>> void setSorted(List<S> sorted) {
    }
}
