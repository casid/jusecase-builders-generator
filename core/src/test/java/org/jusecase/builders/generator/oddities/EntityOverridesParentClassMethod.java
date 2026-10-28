package org.jusecase.builders.generator.oddities;

public class EntityOverridesParentClassMethod extends ParentClass {
    @Override
    public String getParentValue() {
        return "bla";
    }

    @Override
    public void setParentValue(String parentValue) {
        super.setParentValue(parentValue);
    }
}
