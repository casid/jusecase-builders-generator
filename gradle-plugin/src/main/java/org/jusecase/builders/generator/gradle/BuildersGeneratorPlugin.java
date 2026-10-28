package org.jusecase.builders.generator.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskProvider;
import org.jusecase.builders.generator.LineSeparator;

public class BuildersGeneratorPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        BuildersGeneratorExtension extension = project.getExtensions().create("buildersGenerator", BuildersGeneratorExtension.class);
        extension.getTargetDirectory().convention(project.getLayout().getBuildDirectory().dir("generated/sources/jusecase-builders/test"));
        extension.getNestedClasses().convention(true);
        extension.getLineSeparator().convention(LineSeparator.System);

        project.getPlugins().withType(JavaPlugin.class, javaPlugin -> {
            SourceSet test = project.getExtensions().getByType(JavaPluginExtension.class).getSourceSets().getByName(SourceSet.TEST_SOURCE_SET_NAME);

            TaskProvider<GenerateBuildersTask> generate = project.getTasks().register("generateBuilders", GenerateBuildersTask.class, task -> {
                task.setGroup("build");
                task.setDescription("Generates builders for descriptive unit test setups.");
                task.getClasspath().from(test.getCompileClasspath());
                task.getTargetDirectory().set(extension.getTargetDirectory());
                task.getPackages().set(extension.getPackages());
                task.getClasses().set(extension.getClasses());
                task.getExcludeClasses().set(extension.getExcludeClasses());
                task.getBaseClasses().set(extension.getBaseClasses());
                task.getNestedClasses().set(extension.getNestedClasses());
                task.getLineSeparator().set(extension.getLineSeparator());
            });

            // Like the Maven plugin, generate into the test sources. The test compile classpath (main classes
            // and dependencies) is used, since the test output depends on the generated builders itself.
            test.getJava().srcDir(generate.map(GenerateBuildersTask::getTargetDirectory));
        });
    }
}
