# PluginBase

PluginBase is an extensive library designed to streamline the creation of Spigot plugins. This
library provides a solid foundation to build upon, making plugin development more efficient and
manageable.

The majority of my public Spigot
plugins ([CommandButtons](https://github.com/demengc/CommandButtons), [RankGrant+](https://github.com/demengc/RankGrantPlus), [UltraRepair](https://github.com/demengc/UltraRepair),
etc.) and private projects are using PluginBase!

## Modules

### pluginbase-core

The main module containing the core functionality of PluginBase. A rough, incomplete overview of the
features is as follows:

- Schedulers (async/sync runnables, delayed/repeated tasks, etc.)
- Runtime dependency loading
- Text utilities (i.e. colorizing strings with HEX and Adventure support, centered messages,
  components,
  etc.)
- Localization/translation system
- Event handling and listener registration
- Serialization from immutable equivalents of Bukkit objects to YAML
- Custom GUIs with deserializable buttons
- Cooldown maps
- Chat input requests
- Item builder
- GSON provider
- Custom YAML configuration files
- SpigotMC resource update checker
- Cross-version compatibility utilities

### pluginbase-games

A framework for creating minigames. Uses a state-based system to allow for efficient handling of
games that are composed of multiple stages (i.e. preparation phase, fighting phase, celebration
phase, etc.).

### pluginbase-sql

A module for handling SQL databases using the connection pooling
library [HikariCP](https://github.com/brettwooldridge/HikariCP). Some features include connection
management, credential deserialization, statement handlers (prepared statements), result set
handlers, and sync/async queries/executions.

### pluginbase-mongo

A simple module that allows for easy connections to MongoDB databases.

### pluginbase-redis

A module that faciliates cross-server communication using Redis publish-subscribe messaging. Capable
of handling objects.

### PluginBase-Lamp

Though technically a separate project, PluginBase-Lamp is an advanced command library that is
frequently used alongside PluginBase. PluginBase-Lamp is available
at https://github.com/demengc/PluginBase-Lamp.

## Getting Started

To use PluginBase in your project, add the JitPack repository and the module(s) you require to your
build file. An example for the `pluginbase-core` module has been provided below.

### Maven (pom.xml)

```xml

<repositories>
  <repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
  </repository>
</repositories>
```

```xml

<dependencies>
  <dependency>
    <groupId>com.github.demengc.PluginBase</groupId>
    <artifactId>pluginbase-core</artifactId>
    <version>[VERSION]</version>
  </dependency>
</dependencies>
```

### Gradle - Groovy (build.gradle)

```groovy
repositories {
    maven { url = 'https://jitpack.io' }
}
```

```groovy
dependencies {
    implementation 'com.github.demengc.PluginBase:pluginbase-core:[VERSION]'
}
```

### Gradle - Kotlin (build.gradle.kts)

```kotlin
repositories {
    maven(url = "https://jitpack.io")
}
```

```kotlin
dependencies {
    implementation("com.github.demengc.PluginBase:pluginbase-core:[VERSION]")
}
```

## Contributing

Contributions are welcome! If you have suggestions for improvements or find bugs, feel free to
create a pull request.

## License

This project is licensed under the MIT License. For more details, refer to the LICENSE.txt file in
the repository.

## Special Thanks

A special thank you to [lucko](https://github.com/lucko/helper) for creating helper, a major part of
this library. Your contributions have been invaluable to this project.

Another special thank you to [CryptoMorin](https://github.com/CryptoMorin/XSeries) for creating
XSeries, a library that enables PluginBase's cross-version compatibility.

## Support

If you need help or have any questions, you can create an issue in this repository or find me on
my [Discord server](https://demeng.dev/discord). While support for PluginBase is limited, I'll do my
best to assist where possible.
