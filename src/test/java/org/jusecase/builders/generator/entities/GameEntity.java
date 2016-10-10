package org.jusecase.builders.generator.entities;

@SuppressWarnings("unused")
public class GameEntity {
    private Vector position;
    private String name;

    public Vector getPosition() {
        return position;
    }

    public void setPosition(Vector position) {
        this.position = position;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
