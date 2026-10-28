package org.jusecase.builders.generator;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.*;

public class BuildersGenerator {
    private final ClassLoader classLoader;
    private final File targetDirectory;
    private final String[] packages;
    private final String[] classes;
    private final Set<String> excludeClasses;
    private final List<Class<?>> baseClasses;
    private final boolean  nestedClasses;
    private final String lineSeparator;

    public BuildersGenerator(ClassLoader classLoader, File targetDirectory, String[] packages, String[] classes, String[] excludeClasses, String[] baseClasses, boolean nestedClasses, String lineSeparator) {
        this.classLoader = classLoader;
        this.targetDirectory = targetDirectory;
        this.packages = packages;
        this.classes = classes;
        this.excludeClasses = excludeClasses == null ? Collections.emptySet() : new HashSet<>(Arrays.asList(excludeClasses));
        this.nestedClasses = nestedClasses;
        this.lineSeparator = lineSeparator;
        this.baseClasses = loadBaseClasses(classLoader, baseClasses);
    }

    public void generate() {
        Set<Class<?>> classes = new HashSet<>();
        addPackageClasses(classes);
        addSpecificClasses(classes);

        generateBuilders(classes);
    }

    private void generateBuilders(Set<Class<?>> classes) {
        classes.forEach(this::generateBuilder);
    }

    private void generateBuilder(Class<?> clazz) {
        BuilderGenerator generator = new BuilderGenerator(clazz, "UTF-8", lineSeparator);
        if (!generator.isBuildable()) {
            return;
        }

        String packageName = clazz.getPackage().getName();
        File packageDirectory = new File(targetDirectory, packageName.replace('.', File.separatorChar));
        File classFile = new File(packageDirectory, generator.getBuilderClassName() + ".java");

        try {
            packageDirectory.mkdirs();
            classFile.createNewFile();
            generator.generate(new FileOutputStream(classFile));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void addPackageClasses(Set<Class<?>> classes) {
        if (packages == null) {
            return;
        }

        for (String packageName : packages) {
            addPackageClasses(classes, packageName);
        }
    }

    private void addPackageClasses(Set<Class<?>> classes, String packageName) {
        try {
            ImmutableSet<ClassPath.ClassInfo> classesInfo = ClassPath.from(classLoader).getTopLevelClassesRecursive(packageName);
            for (ClassPath.ClassInfo classInfo : classesInfo) {
                if (excludeClasses.contains(classInfo.getName())) {
                    continue;
                }

                Class<?> clazz = classInfo.load();

                if (isWrongBaseClass(clazz)) {
                    continue;
                }

                classes.add(clazz);

                if (nestedClasses) {
                    addNestedClasses(classes, clazz);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to find classes in package '" + packageName + "'", e);
        }
    }

    private void addSpecificClasses(Set<Class<?>> classes) {
        if (this.classes == null) {
            return;
        }

        for (String className : this.classes) {
            addSpecificClass(classes, className);
        }
    }

    private void addSpecificClass(Set<Class<?>> classes, String className) {
        try {
            Class<?> clazz = classLoader.loadClass(className);
            classes.add(clazz);

            if (nestedClasses) {
                addNestedClasses(classes, clazz);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void addNestedClasses(Set<Class<?>> classes, Class<?> clazz) {
        for (Class<?> nestedClass : clazz.getDeclaredClasses()) {
            if (Modifier.isStatic(nestedClass.getModifiers())) {
                classes.add(nestedClass);
            }
        }
    }

    private List<Class<?>> loadBaseClasses(ClassLoader classLoader, String[] baseClasses) {
        if (baseClasses == null || baseClasses.length == 0) {
            return Collections.emptyList();
        }

        List<Class<?>> result = new ArrayList<>();

        for (String baseClassName : baseClasses) {
            try {
                Class<?> baseClass = classLoader.loadClass(baseClassName);
                result.add(baseClass);
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException("Failed to load base class " + baseClassName, e);
            }
        }

        return result;
    }

    private boolean isWrongBaseClass(Class<?> clazz) {
        if (baseClasses.isEmpty()) {
            return false;
        }

        for (Class<?> baseClass : baseClasses) {
            if (baseClass.isAssignableFrom(clazz)) {
                return false;
            }
        }

        return true;
    }
}
