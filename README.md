# Etendo Testing Plugin

The Etendo Testing Plugin is a Gradle plugin designed to facilitate testing in projects using Java and Groovy. 
This plugin provides configurations for source sets and dependencies, making it easier to manage and run tests in Etendo ERP.

## Plugin Configuration

To use the Etendo Testing Plugin, include the following in your `build.gradle` file:

```groovy
plugins {
    id 'com.etendoerp.testing.gradleplugin' version '<version>'
}
```

## Using the plugin in source format

1. Clone the repository into your etendo project 
   ``` shell
   git clone git@github.com:etendosoftware/com.etendoerp.testing.gradleplugin.git buildSrc
   ```
   Then, go to the `build.gradle` file of Etendo and comment the version of the plugin.

2. To compile and deploy an Etendo Core instance you have to setup the configuration variables, to do that you have to create a copy of `gradle.properties.template` file.
    ```bash
    cp gradle.properties.template gradle.properties
    ```
   You can edit `gradle.properties` file updating the variables or use the default values

> To configure GitHub credentials read [Using repositories on Etendo](https://docs.etendo.software/developer-guide/etendo-classic/getting-started/installation/use-of-repositories-in-etendo/)


## Publishing a New Version
To publish a new version of the plugin, follow these steps:  
1. Update the version in your build.gradle file:  
    ```
    version = '<new-version>' // Update to the new version
    ```

2. Run the following Gradle command to publish the plugin:  
    ```
    ./gradlew publish
    ```
   This will build and publish the new version of the plugin to the specified Maven repository.