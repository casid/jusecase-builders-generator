package org.jusecase.builders.generator.entities;

import java.util.Date;

@SuppressWarnings("unused")
public class User {
    private String name;
    private Date creationDate;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }
}
