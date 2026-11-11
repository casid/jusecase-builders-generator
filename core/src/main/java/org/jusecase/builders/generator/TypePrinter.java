package org.jusecase.builders.generator;

import com.google.common.reflect.TypeToken;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Prints types as source code for generated builders. Type variables of the entity become type parameters of the
 * builder interface, and those of a generic setter become type parameters of the builder method. Other type variables
 * have no counterpart in the builder, e.g. of a raw parent class, so the erasure of the whole type is used for them.
 */
final class TypePrinter {
    private static final Set<String> BUILDER_TYPE_PARAMETERS = Set.of("T", "B");

    private final Class<?> entityClass;
    private final Map<String, String> entityVariableNames;
    private final Map<String, TypeVariable<?>> methodVariables;
    private final Map<String, String> methodVariableNames;

    TypePrinter(Class<?> entityClass) {
        this(entityClass, entityVariableNames(entityClass), Collections.emptyMap(), Collections.emptyMap());
    }

    private TypePrinter(Class<?> entityClass, Map<String, String> entityVariableNames, Map<String, TypeVariable<?>> methodVariables, Map<String, String> methodVariableNames) {
        this.entityClass = entityClass;
        this.entityVariableNames = entityVariableNames;
        this.methodVariables = methodVariables;
        this.methodVariableNames = methodVariableNames;
    }

    private static Map<String, String> entityVariableNames(Class<?> entityClass) {
        List<String> names = new ArrayList<>();
        for (TypeVariable<?> variable : entityClass.getTypeParameters()) {
            names.add(variable.getName());
        }
        return uniqueNames(names, BUILDER_TYPE_PARAMETERS);
    }

    /**
     * @return the names mapped to themselves, or to a new name if they are reserved
     */
    private static Map<String, String> uniqueNames(Collection<String> names, Set<String> reserved) {
        Set<String> taken = new HashSet<>(reserved);
        taken.addAll(names);

        Map<String, String> uniqueNames = new LinkedHashMap<>();
        for (String name : names) {
            uniqueNames.put(name, reserved.contains(name) ? uniqueName(name, taken) : name);
        }
        return uniqueNames;
    }

    private static String uniqueName(String name, Set<String> taken) {
        int suffix = 1;
        while (!taken.add(name + suffix)) {
            suffix++;
        }
        return name + suffix;
    }

    /**
     * @return the erasure of the type, if it contains type variables that have no counterpart in the builder
     */
    Type printableType(Type type) {
        return collectVariables(type).values().stream().anyMatch(this::isForeign) ? TypeToken.of(type).getRawType() : type;
    }

    /**
     * @return a printer for the given printable type of a property, aware of the type variables of its generic setter
     */
    TypePrinter forType(Type type) {
        Map<String, TypeVariable<?>> variables = new LinkedHashMap<>();
        for (TypeVariable<?> variable : collectVariables(type).values()) {
            if (!(variable.getGenericDeclaration() instanceof Class<?>)) {
                variables.put(variable.getName(), variable);
            }
        }

        Set<String> reserved = new HashSet<>(BUILDER_TYPE_PARAMETERS);
        reserved.addAll(entityVariableNames.values());
        return new TypePrinter(entityClass, entityVariableNames, variables, uniqueNames(variables.keySet(), reserved));
    }

    private boolean isForeign(TypeVariable<?> variable) {
        return variable.getGenericDeclaration() instanceof Class<?> && variable.getGenericDeclaration() != entityClass;
    }

    private static Map<String, TypeVariable<?>> collectVariables(Type type) {
        Map<String, TypeVariable<?>> variables = new LinkedHashMap<>();
        collectVariables(type, variables);
        return variables;
    }

    private static void collectVariables(Type type, Map<String, TypeVariable<?>> variables) {
        if (type instanceof TypeVariable<?>) {
            TypeVariable<?> variable = (TypeVariable<?>) type;
            // Compared by declaration and name, since resolved type variables may not be equal to the declared ones
            String key = variable.getGenericDeclaration() + "#" + variable.getName();
            if (variables.putIfAbsent(key, variable) == null && !(variable.getGenericDeclaration() instanceof Class<?>)) {
                for (Type bound : variable.getBounds()) {
                    collectVariables(bound, variables);
                }
            }
        } else if (type instanceof GenericArrayType) {
            collectVariables(((GenericArrayType) type).getGenericComponentType(), variables);
        } else if (type instanceof WildcardType) {
            WildcardType wildcard = (WildcardType) type;
            for (Type bound : wildcard.getLowerBounds()) {
                collectVariables(bound, variables);
            }
            for (Type bound : wildcard.getUpperBounds()) {
                collectVariables(bound, variables);
            }
        } else if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            if (parameterizedType.getOwnerType() != null) {
                collectVariables(parameterizedType.getOwnerType(), variables);
            }
            for (Type argument : parameterizedType.getActualTypeArguments()) {
                collectVariables(argument, variables);
            }
        }
    }

    /**
     * @return the type parameters of the entity to append to those of the builder interface, e.g. ", V extends Number"
     */
    String printEntityTypeParameters() {
        return Arrays.stream(entityClass.getTypeParameters())
                .map(variable -> ", " + printTypeParameter(variable))
                .collect(Collectors.joining());
    }

    /**
     * @return the type arguments for the entity in the builder interface, e.g. "&lt;V&gt;"
     */
    String printEntityTypeArguments() {
        if (entityVariableNames.isEmpty()) {
            return "";
        }
        return "<" + String.join(",", entityVariableNames.values()) + ">";
    }

    /**
     * @return the type parameters of the builder method, e.g. "&lt;X&gt; "
     */
    String printMethodTypeParameters() {
        if (methodVariables.isEmpty()) {
            return "";
        }
        return methodVariables.values().stream().map(this::printTypeParameter).collect(Collectors.joining(",", "<", "> "));
    }

    private String printTypeParameter(TypeVariable<?> variable) {
        String bounds = Arrays.stream(variable.getBounds())
                .filter(bound -> bound != Object.class)
                .map(this::print)
                .collect(Collectors.joining(" & "));
        return bounds.isEmpty() ? printVariable(variable) : printVariable(variable) + " extends " + bounds;
    }

    /**
     * @return the type of a builder method parameter, with arrays as varargs
     */
    String printParameter(Type type) {
        if (isArray(type)) {
            return print(getArrayElementType(type)) + " ...";
        }
        return print(type);
    }

    private String print(Type type) {
        if (type instanceof TypeVariable<?>) {
            return printVariable((TypeVariable<?>) type);
        }
        if (type instanceof ParameterizedType) {
            return printParameterizedType((ParameterizedType) type);
        }
        if (type instanceof WildcardType) {
            return printWildcard((WildcardType) type);
        }
        if (isArray(type)) {
            return print(getArrayElementType(type)) + "[]";
        }
        return ((Class<?>) type).getCanonicalName();
    }

    private String printVariable(TypeVariable<?> variable) {
        if (variable.getGenericDeclaration() == entityClass) {
            return entityVariableNames.get(variable.getName());
        }
        return methodVariableNames.get(variable.getName());
    }

    private String printParameterizedType(ParameterizedType type) {
        Class<?> rawType = (Class<?>) type.getRawType();
        String name = type.getOwnerType() instanceof ParameterizedType
                ? print(type.getOwnerType()) + "." + rawType.getSimpleName()
                : print(rawType);
        if (type.getActualTypeArguments().length == 0) {
            return name;
        }
        return name + "<" + Arrays.stream(type.getActualTypeArguments()).map(this::print).collect(Collectors.joining(",")) + ">";
    }

    private String printWildcard(WildcardType wildcard) {
        if (wildcard.getLowerBounds().length > 0) {
            return "? super " + print(wildcard.getLowerBounds()[0]);
        }
        Type upperBound = wildcard.getUpperBounds()[0];
        if (upperBound == Object.class) {
            return "?";
        }
        return "? extends " + print(upperBound);
    }

    private static boolean isArray(Type type) {
        return type instanceof GenericArrayType || (type instanceof Class<?> && ((Class<?>) type).isArray());
    }

    private static Type getArrayElementType(Type type) {
        if (type instanceof GenericArrayType) {
            return ((GenericArrayType) type).getGenericComponentType();
        }
        return ((Class<?>) type).getComponentType();
    }
}
