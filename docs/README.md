---
description: Spigot plugin framework with DI, commands, menus, schedulers, and cross-version support back to Minecraft 1.8.8.
---

# Introduction

PluginBase streamlines Spigot plugin development by providing ready-to-use components that eliminate boilerplate code and enforce best practices.

## Core Features

* **Dependency Injection** - Simple DI with @Component annotation
* **Events** - Functional event handling
* **Commands** - Annotation-driven command framework (Lamp)
* **Text & Localization** - HEX colors, MiniMessage, i18n
* **Schedulers** - Sync/async tasks with Promise API
* **Configuration** - YAML config management
* **Terminables** - Automatic resource lifecycle
* **Menus** - Inventory GUIs with buttons
* **ItemBuilder** - Fluent item creation
* **Cooldowns** - Player/action cooldown tracking
* **Utilities** - Common helpers (Players, Locations, Sounds, etc.)
* **Cross-Version Compatibility** - [XSeries](https://github.com/CryptoMorin/XSeries)

## Optional Modules

* **pluginbase-sql** - HikariCP SQL database
* **pluginbase-mongo** - MongoDB integration
* **pluginbase-redis** - Redis pub/sub messaging
* **pluginbase-games** - Minigame state framework

## Version Compatibility

Minecraft 1.8.8+. Java 17+.

Adventure-based APIs (`Text.parseMini`, `Text.tellComponent`, `mini:` message prefix) require
Paper at runtime and throw `UnsupportedOperationException` on vanilla Spigot. All other features
work on both Paper and Spigot.

## Quick Start

See [Getting Started](overview/getting-started.md) for installation instructions and your first plugin.
