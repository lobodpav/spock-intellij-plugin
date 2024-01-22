# Spock IntelliJ Plugin

Provides support for the amazing [Spock testing framework](https://spockframework.org).
               
# Features

- Validation of block names, their order and completeness (e.g. invalid blocks, missing `then` blocks, etc.)
                        
# Installation

In IntelliJ, go to `Settings -> Plugins` and search for `spock` in the Marketplace.

For more details, see the [plugin page](https://plugins.jetbrains.com/plugin/23380-spock-framework-support). 

# Development

1. Generate a private key and a signing certificate. Refer to the [official documentation](https://plugins.jetbrains.com/docs/intellij/plugin-signing.html#generate-private-key).
2. Define environment properties.
    ```shell
    export INTELLIJ_COMMUNITY_SOURCES = "A path to checked out GIT source codes of the IntelliJ Community"
    export INTELLIJ_CERTIFICATE_CHAIN = "The content of the certificate"
    export INTELLIJ_PRIVATE_KEY       = "The content of the private key"
    export INTELLIJ_PRIVATE_PASSWORD  = "The password to the private key"
    ```
3. Run `gradle runIde` to execute an IntelliJ instance with the plugin installed.  
                
# Verification
              
Verifies the plugin, its configuration and binary compatibility.
     
```shell
gradle runPluginVerifier
```

# Signing
                                                
```shell
gradle signPlugin
```

# Release

1. Increment the version in [build.gradle.kts](build.gradle.kts)
2. Update
   - [CHANGELOG.md](CHANGELOG.md)
   - [plugin.xml](src/main/resources/META-INF/plugin.xml)
3. Update the [CHANGELOG.md](CHANGELOG.md)
4. Run `op run -- gradle build runPluginVerifier signPlugin publishPlugin`

## Security

All the signing credentials (key, certificate, password) are configured
via environment variables (see the [build.gradle.kts](build.gradle.kts) file).

To avoid storing plain text secrets on the filesystem, [1password CLI](https://developer.1password.com/docs/cli)
can be utilised to inject the credentials to the Gradle process. 

```shell
op run -- gradle signPlugin
```

# Implementation details
 
## IntelliJ detection of Spock specs

A green `run` icon appears when a spock block is present in a method
- See the `org.jetbrains.plugins.groovy.ext.spock.SpockTestFramework` class
- See the `plugins/Groovy/lib/Groovy.jar!/META-INF/spock-support.xml`
              
## IntelliJ debug logs for the plugin

To obtain `debug` logs generated by the plugin:
1. Go to `Help -> Diagnostic Tools -> Debug Log Settings`.
2. Enter `io.github.lobodpav.spock` into the text area.
3. Open the `build/idea-sandbox/system/log/idea.log` to see the logs.

See https://plugins.jetbrains.com/docs/intellij/ide-infrastructure.html#logging for more details.
                
## IntelliJ threading rules

Write actions (such as creating an editor for a loaded file in a test) must be executed on the UI thread only.
                                                                        
To perform a write action in a test, wrap your code inside the `Application.invokeAndWait()` closure.
```kotlin
ApplicationManager.getApplication().invokeAndWait {
    WriteCommandAction.runWriteCommandAction(project) {
        // Your code goes here
    }
}
```

See [IntelliJ threading rules](https://plugins.jetbrains.com/docs/intellij/general-threading-rules.html)
document for more details.

## IntelliJ UI tests

According to several answers on the [IntelliJ community](https://intellij-support.jetbrains.com/hc/en-us),
instead of UI tests developers should test the business code instead.

For example, rather than testing a custom File creation action by showing a UI dialog and interacting with it,
the Action itself should be unit tested.
