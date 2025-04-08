package com.etendoerp.testing

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test

class TestConfigPlugin implements Plugin<Project> {

    /**
     * Applies the test configuration plugin to the given project.
     * This includes configuring source sets, dependencies, and test tasks.
     *
     * @param project The project to apply the plugin to.
     */
    void apply(Project project) {
        configureSourceSets(project)
        configureDependencies(project)
        configureTestTask(project)
    }

    /**
     * Configures the source sets for the test source set.
     * This includes setting up directories for Java, Groovy, and resources.
     */
    private void configureSourceSets(Project project) {
        project.sourceSets {
            test {
                java {
                    srcDirs 'src-test/src'
                    outputDir = project.file("src-test/build/classes")
                }
                resources {
                    srcDirs 'src-test/resources'
                }
                groovy {
                    srcDirs "src-test/test/groovy"
                    outputDir = project.file("src-test/build/classes")
                }
            }
        }

        // Dynamic source set configuration for 'modules' directory
        if (project.file('modules').exists() && project.file('modules').isDirectory()) {
            project.file('modules').eachDir { dir ->
                project.sourceSets.test.java.srcDirs += "${dir}/src-test/src"
                project.sourceSets.test.resources.srcDirs += "${dir}/src-test/resources"
                project.sourceSets.test.groovy.srcDirs += "${dir}/src-test/test/groovy"
            }
        }

        // Dynamic source set configuration for 'modules_core' directory
        if (project.file('modules_core').exists() && project.file('modules_core').isDirectory()) {
            project.file('modules_core').eachDir { dir ->
                project.sourceSets.test.java.srcDirs += "${dir}/src-test"
                project.sourceSets.test.resources.srcDirs += "${dir}/src-test/resources"
                project.sourceSets.test.groovy.srcDirs += "${dir}/src-test/test/groovy"
            }
        }

        project.sourceSets.test.java.outputDir = project.file("src-test/build/classes")
        project.sourceSets.test.groovy.outputDir = project.file("src-test/build/classes")
    }

    /**
     * Configures the dependencies for the test source set.
     * This includes Spock, JUnit, Mockito, and other testing libraries.
     */
    private void configureDependencies(Project project) {
        project.configurations {
            antClasspath
        }

        project.dependencies {
            testImplementation 'org.codehaus.groovy:groovy-all:3.0.9'
            testImplementation platform("org.spockframework:spock-bom:2.0-M4-groovy-3.0")
            testImplementation 'org.spockframework:spock-core'
            testImplementation 'org.spockframework:spock-junit4'
            testImplementation 'junit:junit:4.13.1'
            testImplementation 'org.mockito:mockito-core:5.0.0'
            testImplementation("com.athaydes:spock-reports:2.0-groovy-3.0") {
                transitive = false
            }
            testImplementation project.fileTree(project.projectDir) {
                include "lib/test/*.jar"
            }
            antClasspath('org.apache.ant:ant-junit:1.9.2') { transitive = false }
            testRuntimeOnly 'org.junit.vintage:junit-vintage-engine'
            testImplementation 'org.mockito:mockito-junit-jupiter:5.2.0'
        }

        project.task('depsTest') {
            doLast {
                project.configurations.compileClasspath.getFiles().each { file ->
                    project.dependencies.testImplementation project.files(file)
                }
            }
        }
    }

    /**
     * Configures the test task to use JUnit Platform and sets system properties.
     * Also configures the Ant task for JUnit.
     */
    private void configureTestTask(Project project) {
        project.tasks.withType(Test).configureEach {
            useJUnitPlatform()
            systemProperty 'com.athaydes.spockframework.report.showCodeBlocks', true
            maxParallelForks = 1
            forkEvery = 100
            maxHeapSize = '2G'
        }

        project.ant.taskdef(
                name: 'junit',
                classname: 'org.apache.tools.ant.taskdefs.optional.junit.JUnitTask',
                classpath: project.configurations.antClasspath.asPath
        )
    }
}
