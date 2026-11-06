package org.jusecase.builders.generator;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BuildersGenerator {
    private static final int MAX_REPORTED_FILES = 25;
    private static final String GENERATED_MARKER = "@Generated(\"jusecase-builders-generator\")";

    private final ClassLoader classLoader;
    private final File targetDirectory;
    private final String[] packages;
    private final String[] classes;
    private final Set<String> excludeClasses;
    private final List<Class<?>> baseClasses;
    private final boolean  nestedClasses;
    private final String lineSeparator;
    private final Logger logger;

    public BuildersGenerator(ClassLoader classLoader, File targetDirectory, String[] packages, String[] classes, String[] excludeClasses, String[] baseClasses, boolean nestedClasses, String lineSeparator, Logger logger) {
        this.classLoader = classLoader;
        this.targetDirectory = targetDirectory;
        this.packages = packages;
        this.classes = classes;
        this.excludeClasses = excludeClasses == null ? Collections.emptySet() : new HashSet<>(Arrays.asList(excludeClasses));
        this.nestedClasses = nestedClasses;
        this.lineSeparator = lineSeparator;
        this.logger = logger;
        this.baseClasses = loadBaseClasses(classLoader, baseClasses);
    }

    /**
     * Creates the class loader to scan, containing exactly the given classpath. The parent is the platform loader, not
     * the caller's loader: the project's own Guava, builders etc. must win over the generator's copies, and scanning
     * must not pick up the generator's classpath.
     */
    public static URLClassLoader createClassLoader(Collection<File> classpath) {
        List<URL> urls = new ArrayList<>();
        for (File file : classpath) {
            try {
                urls.add(file.toURI().toURL());
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException("Invalid classpath element " + file, e);
            }
        }
        return new URLClassLoader(urls.toArray(new URL[0]), ClassLoader.getPlatformClassLoader());
    }

    public void generate() {
        warnAboutUnknownExcludes();

        Set<Class<?>> classes = new HashSet<>();
        addPackageClasses(classes);
        addSpecificClasses(classes);

        // Everything that can fail is done before touching the target directory, so a failing run
        // doesn't leave it without builders.
        Map<File, byte[]> builders = renderBuilders(classes);

        cleanTargetDirectory();
        writeBuilders(builders);
    }

    private void warnAboutUnknownExcludes() {
        for (String className : excludeClasses) {
            if (classLoader.getResource(className.replace('.', '/') + ".class") == null) {
                logger.warn("excludeClasses entry '" + className + "' was not found on the builders generator classpath and has no effect. "
                        + "By default only production classes are on it.");
            }
        }
    }

    /**
     * Removes builders of earlier runs from the target directory, so no stale builders of removed or excluded
     * classes are left. The directory must only contain generated builders: if anything else is found, nothing is
     * deleted and generation fails. Hidden files and directories (starting with a dot, like .DS_Store or .gitkeep)
     * are ignored and kept. Symbolic links are not supported and fail generation.
     */
    private void cleanTargetDirectory() {
        Path root = targetDirectory.toPath();
        if (Files.isSymbolicLink(root)) {
            throw new IllegalStateException("Target directory " + root + " is a symbolic link, refusing to clean it.");
        }
        if (!Files.isDirectory(root)) {
            return;
        }

        List<Path> generated = new ArrayList<>();
        List<Path> directories = new ArrayList<>();
        List<Path> foreign = new ArrayList<>();
        List<Path> symlinks = new ArrayList<>();

        try {
            Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    if (dir.equals(root)) {
                        return FileVisitResult.CONTINUE;
                    }
                    if (isHidden(dir)) {
                        return FileVisitResult.SKIP_SUBTREE;
                    }
                    directories.add(dir);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (attrs.isSymbolicLink()) {
                        symlinks.add(file);
                    } else if (isHidden(file)) {
                        return FileVisitResult.CONTINUE;
                    } else if (attrs.isRegularFile() && file.toString().endsWith(".java") && isGenerated(file)) {
                        generated.add(file);
                    } else {
                        foreign.add(file);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });

            if (!symlinks.isEmpty()) {
                throw new IllegalStateException("Target directory " + root + " contains symbolic links, refusing to clean it.\n"
                        + "Symbolic links found:\n" + formatFileList(symlinks));
            }
            if (!foreign.isEmpty()) {
                throw new IllegalStateException("Target directory " + root + " contains files that were not generated by "
                        + "jusecase-builders-generator, refusing to clean it. "
                        + "Keep generated builders in a dedicated directory, separate from other sources. "
                        + "If these files are builders from an older version of the generator, delete them once "
                        + "manually and generate again.\n"
                        + "Files found:\n" + formatFileList(foreign));
            }

            for (Path file : generated) {
                Files.delete(file);
            }
            // Reverse order deletes subdirectories before their parents. Directories that still contain hidden
            // files are kept.
            directories.sort(Comparator.reverseOrder());
            for (Path dir : directories) {
                if (isEmptyDirectory(dir)) {
                    Files.delete(dir);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to clean target directory " + root, e);
        }
    }

    private static String formatFileList(List<Path> files) {
        String fileList = files.stream().limit(MAX_REPORTED_FILES).map(path -> "  " + path).collect(Collectors.joining("\n"));
        if (files.size() > MAX_REPORTED_FILES) {
            fileList += "\n  ... and " + (files.size() - MAX_REPORTED_FILES) + " more";
        }
        return fileList;
    }

    private static boolean isHidden(Path path) {
        return path.getFileName().toString().startsWith(".");
    }

    private static boolean isEmptyDirectory(Path dir) throws IOException {
        try (Stream<Path> entries = Files.list(dir)) {
            return !entries.findAny().isPresent();
        }
    }

    private static boolean isGenerated(Path file) throws IOException {
        return new String(Files.readAllBytes(file), StandardCharsets.UTF_8).contains(GENERATED_MARKER);
    }

    private Map<File, byte[]> renderBuilders(Set<Class<?>> classes) {
        Map<File, byte[]> builders = new LinkedHashMap<>();
        for (Class<?> clazz : classes) {
            BuilderGenerator generator = new BuilderGenerator(clazz, "UTF-8", lineSeparator, logger);
            if (!generator.isBuildable()) {
                continue;
            }

            String packageName = clazz.getPackage().getName();
            File packageDirectory = new File(targetDirectory, packageName.replace('.', File.separatorChar));
            File classFile = new File(packageDirectory, generator.getBuilderClassName() + ".java");

            try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                generator.generate(os);
                builders.put(classFile, os.toByteArray());
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        return builders;
    }

    private void writeBuilders(Map<File, byte[]> builders) {
        for (Map.Entry<File, byte[]> builder : builders.entrySet()) {
            File classFile = builder.getKey();
            try {
                Files.createDirectories(classFile.getParentFile().toPath());
                Files.write(classFile.toPath(), builder.getValue());
            } catch (IOException e) {
                throw new UncheckedIOException("Failed to write " + classFile, e);
            }
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
            throw new IllegalArgumentException("Failed to load class " + className, e);
        }
    }

    private void addNestedClasses(Set<Class<?>> classes, Class<?> clazz) {
        for (Class<?> nestedClass : clazz.getDeclaredClasses()) {
            // The generated builder can't access private classes
            if (Modifier.isStatic(nestedClass.getModifiers()) && !Modifier.isPrivate(nestedClass.getModifiers())
                    && !excludeClasses.contains(nestedClass.getName())) {
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
