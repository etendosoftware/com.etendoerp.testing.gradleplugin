package com.etendoerp.testing

import org.gradle.api.Plugin
import org.gradle.api.Project

class TestConfigPlugin implements Plugin<Project> {
    void apply(Project project) {
        // Define configurations
        project.configurations {
            antClasspath
        }

        // Configure test task
        project.test {
            useJUnitPlatform()
            systemProperty 'com.athaydes.spockframework.report.showCodeBlocks', true
            maxParallelForks 1
            forkEvery 100
            maxHeapSize = '2G'
        }

        // Add dependencies
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

        // Define Ant task
        project.ant.taskdef(
                name: 'junit',
                classname: 'org.apache.tools.ant.taskdefs.optional.junit.JUnitTask',
                classpath: project.configurations.antClasspath.asPath
        )

        // Configure source sets
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
                }
            }
        }

        // Dynamic source set configuration for 'modules' directory
        if (project.file('modules').exists() && project.file('modules').isDirectory()) {
            project.file('modules').eachDir { dir ->
                project.sourceSets.test.java.srcDirs += "${dir}/src-test/src"
                project.sourceSets.test.resources.srcDirs += "${dir}/src-test/resources"
                project.sourceSets.test.java.outputDir = project.file("src-test/build/classes")
                project.sourceSets.test.groovy.srcDirs += "${dir}/src-test/test/groovy"
                project.sourceSets.test.groovy.outputDir = project.file("src-test/build/classes")
            }
        }

        // Dynamic source set configuration for 'modules_core' directory
        if (project.file('modules_core').exists() && project.file('modules_core').isDirectory()) {
            project.file('modules_core').eachDir { dir ->
                project.sourceSets.test.java.srcDirs += "${dir}/src-test"
                project.sourceSets.test.resources.srcDirs += "${dir}/src-test/resources"
                project.sourceSets.test.java.outputDir = project.file("src-test/build/classes")
                project.sourceSets.test.groovy.srcDirs += "${dir}/src-test/test/groovy"
                project.sourceSets.test.groovy.outputDir = project.file("src-test/build/classes")
            }
        }

        // Custom task to handle dependencies
        project.task('depsTest') {
            doLast {
                project.configurations.compileClasspath.getFiles().each { file ->
                    project.dependencies.testImplementation project.files(file)
                }
            }
        }
    }
}