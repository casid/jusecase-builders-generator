package org.jusecase.builders.generator.oddities;

import java.util.Comparator;
import java.util.List;

@SuppressWarnings("unused")
public class GenericEntityWithTypeArguments<V extends Number> {
    public void setArray(V[] array) {
    }

    public void setComparator(Comparator<V> comparator) {
    }

    public void setType(Class<V> type) {
    }

    public void setValues(List<V> values) {
    }
}
