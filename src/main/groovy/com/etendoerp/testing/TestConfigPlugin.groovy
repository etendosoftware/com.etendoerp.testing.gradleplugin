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
                    srcDirs = ['src-test/src']
                }
                resources {
                    srcDirs = ['src-test/resources']
                }
                groovy {
                    srcDirs = ["src-test/test/groovy"]
                    output.classesDirs = project.files("src-test/build/classes")
                }
            }
        }

        // Dynamic source set configuration for 'modules' directory
        if (project.file('modules').exists() && project.file('modules').isDirectory()) {
            project.file('modules').eachDir {
                project.sourceSets.test.java.srcDirs += it.toString() + "/src-test/src"
                project.sourceSets.test.resources.srcDirs += it.toString() + "/src-test/resources"
                project.sourceSets.test.groovy.srcDirs += it.toString() + "/src-test/test/groovy"
            }
        }

        // Dynamic source set configuration for 'modules_core' directory
        if (project.file('modules_core').exists() && project.file('modules_core').isDirectory()) {
            project.file('modules_core').eachDir {
                project.sourceSets.test.java.srcDirs += it.toString() + "/src-test"
                project.sourceSets.test.resources.srcDirs += it.toString() + "/src-test/resources"
                project.sourceSets.test.groovy.srcDirs += it.toString() + "/src-test/test/groovy"
            }
        }

        project.sourceSets.test.java.srcDirs += "src-test/src"
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
            // Groovy dependencies
            testImplementation 'org.codehaus.groovy:groovy-all:3.0.9' // Core Groovy library for testing

            // Spock dependencies for testing
            testImplementation platform("org.spockframework:spock-bom:2.0-M4-groovy-3.0") // BOM for Spock framework
            testImplementation 'org.spockframework:spock-core' // Core Spock testing framework
            testImplementation 'org.spockframework:spock-junit4' // Spock integration with JUnit 4

            // JUnit dependencies
            testImplementation 'junit:junit:4.13.1' // JUnit 4 for unit testing
            testRuntimeOnly 'org.junit.vintage:junit-vintage-engine' // JUnit Vintage for running JUnit 4 tests
            testImplementation 'org.junit.platform:junit-platform-suite-api:1.9.2' // JUnit Platform Suite API
            testRuntimeOnly 'org.junit.platform:junit-platform-suite-engine:1.9.2' // JUnit Platform Suite Engine
            testImplementation 'org.junit.jupiter:junit-jupiter-params:5.9.2' // JUnit Jupiter support for parameterized tests

            // Mockito dependencies
            testImplementation 'org.mockito:mockito-core:5.0.0' // Core Mockito library for mocking
            testImplementation 'org.mockito:mockito-junit-jupiter:5.2.0' // Mockito integration with JUnit 5

            // Reporting dependencies
            testImplementation("com.athaydes:spock-reports:2.0-groovy-3.0") { // Spock reporting library
                transitive = false
            }

            // Local test libraries
            testImplementation project.fileTree(project.projectDir) { // Include local test JARs
                include "lib/test/*.jar"
            }

            // Ant dependencies
            antClasspath('org.apache.ant:ant-junit:1.9.2') { transitive = false } // Ant JUnit task for testing
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

        project.tasks.named('compileTestJava') {
            it.destinationDirectory.set(project.file("src-test/build/classes"))
        }

        project.tasks.named('compileTestGroovy') {
            it.destinationDirectory.set(project.file("src-test/build/classes"))
            it.dependsOn project.tasks.named('compileTestJava')
        }

        project.tasks.named('test') {
            it.classpath += project.files("src-test/build/classes")
            it.jvmArgs '--add-opens', 'java.base/java.lang=ALL-UNNAMED'
        }

        // Ant taskdef for compatibility
        project.ant.taskdef(
                name: 'junit',
                classname: 'org.apache.tools.ant.taskdefs.optional.junit.JUnitTask',
                classpath: project.configurations.antClasspath.asPath
        )
    }
}
