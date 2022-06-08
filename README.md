# LMCIP - Let Me Copy in Peace
Cleans formatting information from clipboard

Small application (tray icon) that monitors the clipboard and removes
formatting from text for easier copying.

## Running

This application only consists of a single class and can be run
directly with the Java executable.

It can also be build into a runnable Jar via gradle.

You can toggle the clipboard monitoring via the tray icon menu or by
double-clicking the icon

### Example
```
java -jar lmcip.jar
```

```
java LMCIP.java
```

You also execute the Jar via your OS directly (e.g. via the file explorer).

## Building

This application uses gradle for building and will create a runnable Jar file with the 'jar' target.

```
gradlew jar
```

The runnable jar file can be found at **build/libs/lmcip.jar**
