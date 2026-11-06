# jusecase-builders-generator

[![Java CI with Maven](https://github.com/casid/jusecase-builders-generator/actions/workflows/maven.yml/badge.svg)](https://github.com/casid/jusecase-builders-generator/actions/workflows/maven.yml)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](https://raw.githubusercontent.com/casid/jusecase-builders-generator/master/LICENSE)
[![Maven Central](https://img.shields.io/maven-central/v/org.jusecase/builders-generator-core.svg)](http://mvnrepository.com/artifact/org.jusecase/builders-generator-core)

Generator for jusecase builders

## Modules

| Module          | Artifact                                       | Description                                               |
|-----------------|------------------------------------------------|-----------------------------------------------------------|
| `core`          | `org.jusecase:builders-generator-core`         | Build tool independent generator                          |
| `maven-plugin`  | `org.jusecase:builders-generator-maven-plugin` | Maven plugin, goal `builders-generator:generate-builders` |
| `gradle-plugin` | `org.jusecase.builders-generator` (plugin id)  | Gradle plugin, task `generateBuilders`                    |

The root `pom.xml` builds `core` and `maven-plugin`. The Gradle plugin is a separate Gradle build that
resolves the core from Maven, so run `mvn install` first, then `./gradlew build` inside `gradle-plugin`.

## Upgrading to 2.0.0

Maven plugin:

- The artifact is now `org.jusecase:builders-generator-maven-plugin`. Version 2.0.0 of the old
  `org.jusecase:builders-generator` relocates to it, so Maven warns and resolves the new artifact.
- Only production classes and their dependencies (compile and runtime classpath) are scanned. Entities in `src/test` no longer get builders, and
  `classes` or `baseClasses` entries that only exist in test code fail with `Failed to load class`.
- Classes on the plugin's own classpath are no longer scanned, so builders generated from them are gone.
- `targetDirectory` is added to the test sources, so a manual `build-helper-maven-plugin` setup for it can be removed.
- A relative `targetDirectory` is resolved against the module instead of the working directory.

Both plugins:

- Builders of earlier runs are deleted before generating, see [Target directory](#target-directory). Runs sharing a
  target directory delete each other's builders, so give each run its own directory.
- Generated builders use Unix line separators by default instead of the system's. Set `lineSeparator` to `System` to
  keep the old behavior.

## Runtime dependency

Generated builders use `org.jusecase:builders`. The plugins don't add it to your project, so declare it as a test
dependency yourself:

```xml
<dependency>
    <groupId>org.jusecase</groupId>
    <artifactId>builders</artifactId>
    <version>1.0.1</version>
    <scope>test</scope>
</dependency>
```

```groovy
dependencies {
    testImplementation 'org.jusecase:builders:1.0.1'
}
```

## Scanned classes

Both plugins scan the production classes and their dependencies (compile and runtime classpath, so types that are only
available at runtime can be resolved) in isolation from the plugin's own classpath.
Test classes are not scanned, since they may depend on the generated builders, so entities in `src/test` don't get
builders. Entries of `excludeClasses` that are not found are warned about. Nested classes are excluded by their binary
name (e.g. `com.foo.Outer$Inner`).

## Properties

Builder methods are generated for public, non-static, non-final fields and public, non-static `setX(value)` methods.
Setters that declare checked exceptions are skipped, since the generated methods don't handle them. Only ASCII names
are supported: fields and setters with other characters in their name are skipped with a warning.

## Generic entities

Type parameters of an entity are appended to those of its generated builder interface, so they have to be provided
when implementing it. They are renamed if they clash, e.g. `T` of the entity becomes `T1` in the builder interface.

```java
class GenBuilder implements GenBuilderMethods<Gen<Integer>, GenBuilder, Integer> {
    // ...
}
```

## Target directory

Builders are generated into a dedicated directory inside the build directory by default
(`target/generated-test-sources/jusecase-builders` for Maven, `build/generated/sources/jusecase-builders/test`
for Gradle) and are not checked in. This is the recommended setup. Both plugins add the directory to the test
sources. A relative `targetDirectory` is resolved against the module or project.

Checked-in builders are supported as well: point `targetDirectory` to a source directory and generate them on demand.
For Maven, run the goal manually instead of binding it to the lifecycle, after compiling the module
(`mvn compile builders-generator:generate-builders`). The goal fails on a module with sources that was not compiled yet.
For Gradle, set `addToTestSources = false`. Then add the directory to your test sources yourself, via the task, so the
test compilation depends on it: `srcDir(tasks.named('generateBuilders').map { it.targetDirectory })`. Use a directory that only contains builders
(e.g. `src/test/builders`), not a shared source root like `src/test/java`.

Since 2.0.0, builders of earlier runs are deleted before generating, so builders of removed or excluded classes
don't linger. Only files marked with `@Generated("jusecase-builders-generator")` are deleted. Hidden files
(starting with a dot, like `.gitkeep`) are ignored. If the directory contains any other file or a symbolic link,
generation fails without deleting anything. Nothing is deleted either if generation fails for another reason (e.g. a
class from `classes` can't be loaded): builders are only replaced once all of them were generated successfully. Builders generated by versions before 1.0.0 are not marked this way
and need to be deleted once manually.

```groovy
plugins {
    id 'org.jusecase.builders-generator'
}

buildersGenerator {
    packages = ['com.example.entities']
}
```

Additional Gradle options:

| Option              | Default                                      | Description                                                                                       |
|---------------------|----------------------------------------------|---------------------------------------------------------------------------------------------------|
| `addToTestSources`  | `true`                                       | Adds `targetDirectory` to the test sources. Set to `false` for checked-in builders.               |
| `classpath`         | main classes and their compile and runtime classpath | Classes the builders are generated from. Entries of `excludeClasses` not found on it are warned about. |
| `lineSeparator`     | `Unix`                                       | `Unix`, `Windows` or `System`. Groovy scripts can assign it as a string.                          |

The generator runs in a separate process using the project's Java toolchain, so classes compiled for a newer Java
version than the one Gradle runs on can be loaded. The task is only build-cacheable while `targetDirectory`
is inside the build directory, so checked-in sources are never overwritten from the cache.
The task only tracks the public API of the scanned classes (compile classpath normalization), so builders are not
regenerated if only method bodies or private members change.
