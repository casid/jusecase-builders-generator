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
| `classpath`         | test compile classpath                       | Classes the builders are generated from. Entries of `excludeClasses` not found on it are warned about. |
| `lineSeparator`     | `System`                                     | `System`, `Windows` or `Unix`. Groovy scripts can assign it as a string.                          |

The generator runs in a separate process using the project's Java toolchain, so classes compiled for a newer Java
version than the one Gradle runs on can be loaded. The task is only build-cacheable while `targetDirectory`
is inside the build directory, so checked-in sources are never overwritten from the cache.
