package org.jusecase.builders.generator.oddities;

@SuppressWarnings("unused")
public class EntityWithGenericParentClass extends GenericMiddleClass<EntityWithGenericParentClass.Item> {
    public static class Item {
    }

    public enum Status {
        OPEN, CLOSED
    }

    public static class Wrapper<V> {
    }

    public void setStatus(Status status) {
    }

    public void setWrapper(Wrapper<Item> wrapper) {
    }
}
