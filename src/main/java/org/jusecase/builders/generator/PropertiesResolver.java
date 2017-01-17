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
        addProperties(entityClass, properties);
        properties.sort(this::sortProperties);
        return properties;
    }

    private int sortProperties(Property o1, Property o2) {
        int result = o1.name.compareTo(o2.name);
        if (result == 0) {
            result = o1.type.getName().compareTo(o2.type.getName());
        }
        return result;
    }

    private void addProperties(Class<?> clazz, List<Property> properties) {
        if (clazz != Object.class) {
            addPublicFields(clazz, properties);
            addSetterMethods(clazz, properties);

            addProperties(clazz.getSuperclass(), properties);
        }
    }

    private void addPublicFields(Class<?> clazz, List<Property> properties) {
        for (Field field : clazz.getDeclaredFields()) {
            if (isSuitableField(field)) {
                Property property = new Property();
                property.name = field.getName();
                property.type = field.getType();
                property.isSetter = false;
                properties.add(property);
            }
        }
    }

    private void addSetterMethods(Class<?> clazz, List<Property> properties) {
        for (Method method : clazz.getDeclaredMethods()) {
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
                return correctNestedTypeName(type.getSimpleName());
            } else {
                return correctNestedTypeName(type.getName());
            }
        }

        private String correctNestedTypeName(String typeName) {
            return typeName.replace('$', '.');
        }

        public String nameStartingWithUppercase() {
            return ("" + name.charAt(0)).toUpperCase() +  name.substring(1);
        }
    }
}
