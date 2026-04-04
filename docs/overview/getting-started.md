---
description: Add PluginBase to your project, shade it, and create your first plugin.
---

# Getting Started

## Add the Dependency

PluginBase is published to [Maven Central](https://central.sonatype.com/namespace/dev.demeng.pluginbase). Check there for the latest version.

{% tabs %}
{% tab title="Maven (pom.xml)" %}
```xml
<dependencies>
  <!-- Core module (required) -->
  <dependency>
    <groupId>dev.demeng.pluginbase</groupId>
    <artifactId>pluginbase-core</artifactId>
    <version>VERSION</version>
  </dependency>

  <!-- Optional modules -->
  <dependency>
    <groupId>dev.demeng.pluginbase</groupId>
    <artifactId>pluginbase-games</artifactId>
    <version>VERSION</version>
  </dependency>
  <dependency>
    <groupId>dev.demeng.pluginbase</groupId>
    <artifactId>pluginbase-mongo</artifactId>
    <version>VERSION</version>
  </dependency>
  <dependency>
    <groupId>dev.demeng.pluginbase</groupId>
    <artifactId>pluginbase-redis</artifactId>
    <version>VERSION</version>
  </dependency>
  <dependency>
    <groupId>dev.demeng.pluginbase</groupId>
    <artifactId>pluginbase-sql</artifactId>
    <version>VERSION</version>
  </dependency>
</dependencies>
```
{% endtab %}

{% tab title="Gradle (build.gradle)" %}
```groovy
dependencies {
    // Required
    implementation 'dev.demeng.pluginbase:pluginbase-core:VERSION'

    // Optional
    implementation 'dev.demeng.pluginbase:pluginbase-games:VERSION'
    implementation 'dev.demeng.pluginbase:pluginbase-mongo:VERSION'
    implementation 'dev.demeng.pluginbase:pluginbase-redis:VERSION'
    implementation 'dev.demeng.pluginbase:pluginbase-sql:VERSION'
}
```
{% endtab %}

{% tab title="Gradle (build.gradle.kts)" %}
```kotlin
dependencies {
    // Required
    implementation("dev.demeng.pluginbase:pluginbase-core:VERSION")

    // Optional
    implementation("dev.demeng.pluginbase:pluginbase-games:VERSION")
    implementation("dev.demeng.pluginbase:pluginbase-mongo:VERSION")
    implementation("dev.demeng.pluginbase:pluginbase-redis:VERSION")
    implementation("dev.demeng.pluginbase:pluginbase-sql:VERSION")
}
```
{% endtab %}
{% endtabs %}

## Shade and Relocate PluginBase

PluginBase must be shaded into your plugin JAR and relocated to avoid conflicts with other plugins that bundle it. Replace `com.example.myplugin.lib.pluginbase` with a package under your own namespace.

{% tabs %}
{% tab title="Maven (pom.xml)" %}
```xml
<build>
  <plugins>
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-shade-plugin</artifactId>
      <version>3.6.1</version>
      <executions>
        <execution>
          <phase>package</phase>
          <goals>
            <goal>shade</goal>
          </goals>
          <configuration>
            <relocations>
              <relocation>
                <pattern>dev.demeng.pluginbase</pattern>
                <shadedPattern>com.example.myplugin.lib.pluginbase</shadedPattern>
              </relocation>
            </relocations>
          </configuration>
        </execution>
      </executions>
    </plugin>
  </plugins>
</build>
```
{% endtab %}

{% tab title="Gradle (build.gradle)" %}
```groovy
plugins {
    id 'com.github.johnrengelman.shadow' version '8.1.1'
    id 'java'
}

shadowJar {
    relocate 'dev.demeng.pluginbase', 'com.example.myplugin.lib.pluginbase'
}
```
{% endtab %}

{% tab title="Gradle (build.gradle.kts)" %}
```kotlin
plugins {
    id("com.github.johnrengelman.shadow") version "8.1.1"
    java
}

tasks.shadowJar {
    relocate("dev.demeng.pluginbase", "com.example.myplugin.lib.pluginbase")
}
```
{% endtab %}
{% endtabs %}

## Preserve Parameter Names for Lamp Commands

Lamp uses method parameter names to derive command argument names. Java does not retain these by default, so you need the `-parameters` compiler flag. Without it, Lamp falls back to generic names like `arg0`.

{% tabs %}
{% tab title="Maven (pom.xml)" %}
```xml
<build>
  <plugins>
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-compiler-plugin</artifactId>
      <version>3.14.1</version>
      <configuration>
        <compilerArgs>
          <arg>-parameters</arg>
        </compilerArgs>
      </configuration>
    </plugin>
  </plugins>
</build>
```
{% endtab %}

{% tab title="Gradle (build.gradle)" %}
```groovy
tasks.withType(JavaCompile) {
    options.compilerArgs << '-parameters'
}
```
{% endtab %}

{% tab title="Gradle (build.gradle.kts)" %}
```kotlin
tasks.withType<JavaCompile> {
    options.compilerArgs.add("-parameters")
}
```
{% endtab %}
{% endtabs %}

## Extend BasePlugin

`BasePlugin` wraps `JavaPlugin` with automatic lifecycle management. Override `enable()` and `disable()` instead of `onEnable()` and `onDisable()` (those are `final` in `BasePlugin`).

```java
package com.example.myplugin;

import dev.demeng.pluginbase.plugin.BasePlugin;
import dev.demeng.pluginbase.Events;
import dev.demeng.pluginbase.text.Text;
import org.bukkit.event.player.PlayerJoinEvent;

public class MyPlugin extends BasePlugin {

  @Override
  protected void enable() {
    Text.console("&aMyPlugin enabled!");

    Events.subscribe(PlayerJoinEvent.class)
        .handler(e -> Text.tell(e.getPlayer(), "&aWelcome!"))
        .bindWith(this);
  }

  @Override
  protected void disable() {
    Text.console("&cMyPlugin disabled!");
  }
}
```

| Method | When it runs | Purpose |
|--------|-------------|---------|
| `load()` | `onLoad` (before enable) | Early initialization, dependency container setup |
| `enable()` | `onEnable` | Register commands, listeners, load configs |
| `disable()` | `onDisable` | Cleanup (most resources auto-cleanup via `bindWith`) |

## Create plugin.yml

```yaml
name: MyPlugin
version: 1.0.0
main: com.example.myplugin.MyPlugin
api-version: 1.13
```

## Configure BaseSettings

Override `BaseSettings` methods to customize library-wide behavior. Call `setBaseSettings()` early in `enable()`.

```java
@Override
protected void enable() {
  setBaseSettings(new BaseSettings() {
    @Override
    public String prefix() {
      return "&8[&bMyPlugin&8]&r ";
    }

    @Override
    public boolean includePrefixOnEachLine() {
      return true;
    }

    @Override
    public ColorScheme colorScheme() {
      return new ColorScheme("&b", "&7", "&f");
    }
  });
}
```

| Method | Default | Description |
|--------|---------|-------------|
| `prefix()` | `"&r"` | Prepended to `Text.tell()` and `Text.console()` messages |
| `includePrefixOnEachLine()` | `true` | Adds the prefix to each `\n`-delimited line |
| `colorScheme()` | `null` | 3-color scheme accessible as `&p`, `&s`, `&t` in any colorized text |
| `dateTimeFormat()` | `"MMMM dd yyyy HH:mm z"` | Format for combined date/time strings |
| `dateFormat()` | `"MMMM dd yyyy"` | Format for date-only strings |
