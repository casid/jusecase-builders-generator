package org.jusecase.builders.generator.entities;

@SuppressWarnings("unused")
public class EntityWithMarker implements EntityMarker {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
