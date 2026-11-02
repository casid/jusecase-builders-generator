package org.jusecase.builders.generator.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.provider.Provider;
import org.gradle.api.specs.Spec;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.jvm.toolchain.JavaToolchainService;
import org.jusecase.builders.generator.LineSeparator;

public class BuildersGeneratorPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        BuildersGeneratorExtension extension = project.getExtensions().create("buildersGenerator", BuildersGeneratorExtension.class);
        extension.getTargetDirectory().convention(project.getLayout().getBuildDirectory().dir("generated/sources/jusecase-builders/test"));
        extension.getAddToTestSources().convention(true);
        extension.getNestedClasses().convention(true);
        extension.getLineSeparator().convention(LineSeparator.System);

        project.getPlugins().withType(JavaPlugin.class, javaPlugin -> {
            SourceSet test = project.getExtensions().getByType(JavaPluginExtension.class).getSourceSets().getByName(SourceSet.TEST_SOURCE_SET_NAME);

            JavaPluginExtension java = project.getExtensions().getByType(JavaPluginExtension.class);
            JavaToolchainService toolchains = project.getExtensions().getByType(JavaToolchainService.class);
            SourceSet main = java.getSourceSets().getByName(SourceSet.MAIN_SOURCE_SET_NAME);
            extension.getClasspath().convention(main.getRuntimeClasspath());

            TaskProvider<GenerateBuildersTask> generate = project.getTasks().register("generateBuilders", GenerateBuildersTask.class, task -> {
                task.setGroup("build");
                task.setDescription("Generates builders for descriptive unit test setups.");
                task.getClasspath().from(extension.getClasspath());
                task.getJavaLauncher().convention(toolchains.launcherFor(java.getToolchain()));
                // Never restore from the build cache into a directory outside of build/ (e.g. checked-in sources).
                // The spec only holds a provider, so it stays serializable for the configuration cache.
                task.getOutputs().cacheIf("target directory is inside the build directory", new InsideBuildDirectory(
                        extension.getTargetDirectory().zip(project.getLayout().getBuildDirectory(),
                                (target, build) -> target.getAsFile().toPath().startsWith(build.getAsFile().toPath()))));
                task.getTargetDirectory().set(extension.getTargetDirectory());
                task.getPackages().set(extension.getPackages());
                task.getClasses().set(extension.getClasses());
                task.getExcludeClasses().set(extension.getExcludeClasses());
                task.getBaseClasses().set(extension.getBaseClasses());
                task.getNestedClasses().set(extension.getNestedClasses());
                task.getLineSeparator().set(extension.getLineSeparator());
            });

            // Like the Maven plugin, generate into the test sources (unless disabled). By default the production
            // classes and their runtime dependencies are scanned, so no cycle with the test compilation arises.
            project.afterEvaluate(p -> {
                if (extension.getAddToTestSources().get()) {
                    test.getJava().srcDir(generate.map(GenerateBuildersTask::getTargetDirectory));
                }
            });
        });
    }

    private static final class InsideBuildDirectory implements Spec<Task> {
        private final Provider<Boolean> insideBuildDirectory;

        InsideBuildDirectory(Provider<Boolean> insideBuildDirectory) {
            this.insideBuildDirectory = insideBuildDirectory;
        }

        @Override
        public boolean isSatisfiedBy(Task task) {
            return insideBuildDirectory.get();
        }
    }
}
