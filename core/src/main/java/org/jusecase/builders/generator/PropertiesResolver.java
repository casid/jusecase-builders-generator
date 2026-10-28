package org.jusecase.builders.generator;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

public class PropertiesResolver {
    private final Class<?> entityClass;

    public PropertiesResolver(Class<?> entityClass) {
        this.entityClass = entityClass;
    }

    public Collection<Property> resolveProperties() {
        SortedSet<Property> properties = new TreeSet<>();
        addProperties(entityClass, properties);
        return properties;
    }

    private void addProperties(Class<?> clazz, Collection<Property> properties) {
        if (clazz != null && clazz != Object.class) {
            addPublicFields(clazz, properties);
            addSetterMethods(clazz, properties);

            addProperties(clazz.getSuperclass(), properties);
        }
    }

    private void addPublicFields(Class<?> clazz, Collection<Property> properties) {
        for (Field field : clazz.getDeclaredFields()) {
            if (isSuitableField(field)) {
                Property property = new Property();
                property.name = field.getName();
                property.type = field.getGenericType();
                property.isSetter = false;
                properties.add(property);
            }
        }
    }

    private void addSetterMethods(Class<?> clazz, Collection<Property> properties) {
        for (Method method : clazz.getDeclaredMethods()) {
            if (isSuitableMethod(method)) {
                Property property = new Property();
                property.name = method.getName().substring(3);
                property.type = method.getGenericParameterTypes()[0];
                property.isSetter = true;
                properties.add(property);
            }
        }
    }

    private boolean isSuitableField(Field field) {
        return Modifier.isPublic(field.getModifiers()) &&
              !Modifier.isStatic(field.getModifiers()) &&
              !Modifier.isFinal(field.getModifiers());
    }

    private boolean isSuitableMethod(Method method) {
        return Modifier.isPublic(method.getModifiers()) &&
                method.getReturnType().equals(void.class) &&
                method.getParameterTypes().length == 1 &&
                method.getName().matches("^set[A-Z].*");
    }

    public static class Property implements Comparable<Property> {
        public String  name;
        public Type    type;
        public boolean isSetter;

        public String getTypeString() {
            return printType(type, true);
        }

        private String printType(Type type) {
            return printType(type, false);
      }

      private String printType( Type type, boolean unwrapArrayToVararg ) {
         if ( isParametrizedType(type) ) {
            ParameterizedType parameterizedType = (ParameterizedType)type;
            String params = Arrays.stream(parameterizedType.getActualTypeArguments()).map(this::printType).collect(Collectors.joining(","));
            return printType(parameterizedType.getRawType()) + "<" + params + ">";
         }
         if (isArray(type)) {
                Type elementType = getArrayElementType(type);
                if (unwrapArrayToVararg) {
               return printType(elementType) + " ...";
            } else {
               return printType(elementType) + "[]";
            }
            } else if (isJavaLangType(type)) {
                return ((Class<?>) type).getSimpleName();
            } else if (isClass(type)) {
                return ((Class<?>) type).getCanonicalName();
            } else {
                return type.getTypeName();
            }
        }

        private boolean isParametrizedType( Type type ) {
         return type instanceof ParameterizedType;
      }private Type getArrayElementType(Type type) {
            return ((Class<?>) type).getComponentType();
        }

        private static boolean isClass(Type type) {
            return type instanceof Class<?>;
        }

        private static boolean isJavaLangType(Type type) {
            if (type instanceof Class<?>) {
                Class<?> clazz = (Class<?>)type;
                 return clazz.getPackage() == null || "java.lang".equals(clazz.getPackage().getName());
            }
            return false;
        }

        private static boolean isArray( Type type ) {
            if (type instanceof Class<?>) {
                Class<?> clazz = (Class<?>)type;
                return clazz.isArray();
            }
            return false;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Property property = (Property) o;

            return name.equals(property.name) && type.equals(property.type);
        }

        @Override
        public int hashCode() {
            int result = name.hashCode();
            result = 31 * result + type.hashCode();
            return result;
        }

        @Override
        public int compareTo(Property o) {
            int result = name.compareTo(o.name);
            if (result == 0) {
                result = type.getTypeName().compareTo(o.type.getTypeName());
            }
            return result;
        }


        public String nameStartingWithUppercase() {
            return ("" + name.charAt(0)).toUpperCase() +  name.substring(1);
        }
    }
}
