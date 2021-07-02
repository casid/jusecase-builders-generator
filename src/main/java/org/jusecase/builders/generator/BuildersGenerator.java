package org.jusecase.builders.generator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;


public class BuildersGenerator {
    private final ClassLoader classLoader;
    private final File targetDirectory;
    private final String[] packages;
    private final String[] classes;
    private final boolean nestedClasses;
    private final String lineSeparator;

    public BuildersGenerator(ClassLoader classLoader, File targetDirectory, String[] packages, String[] classes, boolean nestedClasses, String lineSeparator) {
        this.classLoader = classLoader;
        this.targetDirectory = targetDirectory;
        this.packages = packages;
        this.classes = classes;
        this.nestedClasses = nestedClasses;
        this.lineSeparator = lineSeparator;
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
        if (packages == null || packages.length == 0) {
            return;
        }

        try {
            try (ScanResult scanResult = new ClassGraph()
                  .acceptPackages(packages)
                  .overrideClassLoaders(classLoader)
                  .scan()) {
                for ( ClassInfo classInfo : scanResult.getAllClasses() ) {
                    if ( classInfo.isInnerClass()) {
                        continue;
                    }

                    Class<?> clazz = classInfo.loadClass();
                    classes.add(clazz);

                    if (nestedClasses) {
                        addNestedClasses(classes, clazz);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to find classes in packages '" + Arrays.toString(packages) + "'", e);
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
}
