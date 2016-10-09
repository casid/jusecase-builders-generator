package org.jusecase.builders.generator;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class PropertiesResolver {
    private final Class<?> entityClass;

    public PropertiesResolver(Class<?> entityClass) {
        this.entityClass = entityClass;
    }

    public List<Property> resolveProperties() {
        List<Property> properties = new ArrayList<>();

        for (Field field : entityClass.getDeclaredFields()) {
            if (isSuitableField(field)) {
                Property property = new Property();
                property.name = field.getName();
                property.type = field.getType();
                property.isSetter = false;
                properties.add(property);
            }
        }

        addSetterMethods(properties);
        return properties;
    }

    private void addSetterMethods(List<Property> properties) {
        for (Method method : entityClass.getDeclaredMethods()) {
            if(isSuitableMethod(method)) {
                Property property = new Property();
                property.name = method.getName().substring(3);
                property.type = method.getParameterTypes()[0];
                property.isSetter = true;
                properties.add(property);
            }
        }
    }

    private boolean isSuitableField(Field field) {
        return Modifier.isPublic(field.getModifiers()) &&
                !Modifier.isStatic(field.getModifiers());
    }

    private boolean isSuitableMethod(Method method) {
        return Modifier.isPublic(method.getModifiers()) &&
                method.getReturnType().equals(void.class) &&
                method.getParameterTypes().length == 1 &&
                method.getName().matches("^set[A-Z].*");
    }

    public static class Property {
        public String name;
        public Class<?> type;
        public boolean isSetter;

        public String getTypeString() {
            if (type.getPackage() == null || "java.lang".equals(type.getPackage().getName())) {
                return type.getSimpleName();
            } else {
                return type.getName();
            }
        }

        public String nameStartingWithUppercase() {
            return ("" + name.charAt(0)).toUpperCase() +  name.substring(1);
        }
    }
}
