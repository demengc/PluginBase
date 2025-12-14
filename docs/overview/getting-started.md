# Getting Started

## Installation

Add PluginBase modules to your project through your build tool (e.g., Maven or Gradle). Ensure to replace `VERSION` with the latest version from the [releases page](https://github.com/demengc/PluginBase/releases).

{% tabs %}
{% tab title="Maven (pom.xml)" %}
```xml
<repositories>
  <repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
  </repository>
</repositories>

<dependencies>
<!-- Core module (required) -->
<dependency>
  <groupId>com.github.demengc.PluginBase</groupId>
  <artifactId>pluginbase-core</artifactId>
  <version>VERSION</version>
</dependency>

<!-- Optional modules -->
<dependency>
  <groupId>com.github.demengc.PluginBase</groupId>
  <artifactId>pluginbase-games</artifactId>
  <version>VERSION</version>
</dependency>
<dependency>
  <groupId>com.github.demengc.PluginBase</groupId>
  <artifactId>pluginbase-mongo</artifactId>
  <version>VERSION</version>
</dependency>
<dependency>
  <groupId>com.github.demengc.PluginBase</groupId>
  <artifactId>pluginbase-redis</artifactId>
  <version>VERSION</version>
</dependency>
<dependency>
  <groupId>com.github.demengc.PluginBase</groupId>
  <artifactId>pluginbase-sql</artifactId>
  <version>VERSION</version>
</dependency>
</dependencies>
```
{% endtab %}

{% tab title="Gradle (build.gradle)" %}
```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    // Required
    implementation 'com.github.demengc.PluginBase:pluginbase-core:VERSION'
    
    // Optional
    implementation 'com.github.demengc.PluginBase:pluginbase-games:VERSION'
    implementation 'com.github.demengc.PluginBase:pluginbase-mongo:VERSION'
    implementation 'com.github.demengc.PluginBase:pluginbase-redis:VERSION'
    implementation 'com.github.demengc.PluginBase:pluginbase-sql:VERSION'
}
```
{% endtab %}

{% tab title="Gradle (build.gradle.kts)" %}
```kotlin
repositories {
    maven {
        url = uri("https://jitpack.io")
    }
}

dependencies {
    // Required
    implementation("com.github.demengc.PluginBase:pluginbase-core:VERSION")

    // Optional
    implementation("com.github.demengc.PluginBase:pluginbase-games:VERSION")
    implementation("com.github.demengc.PluginBase:pluginbase-mongo:VERSION")
    implementation("com.github.demengc.PluginBase:pluginbase-redis:VERSION")
    implementation("com.github.demengc.PluginBase:pluginbase-sql:VERSION")
}
```
{% endtab %}
{% endtabs %}

## Shading and Relocation

To avoid dependency conflicts with other plugins, you should shade and relocate PluginBase into your plugin JAR. Ensure to replace the shaded pattern (`com.example.myplugin.lib.pluginbase` ) with a package relevant to your project.

{% tabs %}
{% tab title="Maven (pom.xml)" %}
```xml
<build>
  <plugins>
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-shade-plugin</artifactId>
      <version>3.5.1</version>
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

## Preserving Parameter Names (Optional)

By default, Java does not preserve method parameter names. To enable features such as automatic command metadata, you should compile your project with the `-parameters` flag.

{% tabs %}
{% tab title="Maven (pom.xml)" %}
```xml
<build>
  <plugins>
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-compiler-plugin</artifactId>
      <version>3.11.0</version>
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

## Your First Plugin

### 1. Extend BasePlugin

```java
package com.example.myplugin;

import dev.demeng.pluginbase.plugin.BasePlugin;
import dev.demeng.pluginbase.Events;
import dev.demeng.pluginbase.text.Text;
import org.bukkit.event.player.PlayerJoinEvent;

public class MyPlugin extends BasePlugin {

  @Override
  protected void enable() {
    // Plugin initialization
    Text.console("&aMyPlugin enabled!");

    // Example: subscribe to events
    Events.subscribe(PlayerJoinEvent.class)
        .handler(e -> Text.tell(e.getPlayer(), "&aWelcome!"))
        .bindWith(this);
  }

  @Override
  protected void disable() {
    // Cleanup (most resources auto-cleanup via bindWith)
    Text.console("&cMyPlugin disabled!");
  }
}
```

### 2. Create plugin.yml

```yaml
name: MyPlugin
version: 1.0.0
main: com.example.myplugin.MyPlugin
api-version: 1.13 # Required for multi-version support
```

### 3. Build and Test

Build your plugin JAR and test it out!

## Configuration

Configure PluginBase settings via `BaseSettings` in your main class:

```java
@Override
protected void enable() {
  setBaseSettings(new BaseSettings() {
    // Example: Modifying the plugin's chat prefix.
    @Override
    public String prefix() {
      return "&8[&bMyPlugin&8]&r ";
    }
    // Other settings can be overridden here.
  });
}
```
