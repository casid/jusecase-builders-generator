package org.jusecase.builders.generator.oddities;

import java.util.Map;

@SuppressWarnings("unused")
public class EntityWithOwnerTypes {
    public void setEntry(Map.Entry<String, Integer> entry) {
    }

    public void setGenericInner(GenericOuterClass<String>.GenericInner<Integer> genericInner) {
    }

    public void setInner(GenericOuterClass<String>.Inner inner) {
    }
}
