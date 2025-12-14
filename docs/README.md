---
description: Comprehensive utility library for Spigot plugin development.
---

# Introduction

## What is PluginBase?

PluginBase streamlines Spigot plugin development by providing ready-to-use components that eliminate boilerplate code and enforce best practices.

## Key Features

### Core Features

* **Dependency Injection** - Simple DI with @Component annotation
* **Events** - Functional event handling
* **Commands** - Powerful annotation-driven commands framework (Lamp)
* **Text & Localization** - HEX colors, MiniMessage, i18n
* **Schedulers** - Sync/async tasks with Promise API
* **Configuration** - YAML config management
* **Terminables** - Automatic resource lifecycle
* **Menus** - Inventory GUIs with buttons
* **ItemBuilder** - Fluent item creation
* **Cooldowns** - Player/action cooldown tracking
* **Utilities** - Common helpers (Players, Locations, Sounds, etc.)

### Optional Modules

* **pluginbase-sql** - HikariCP SQL database
* **pluginbase-mongo** - MongoDB integration
* **pluginbase-redis** - Redis pub/sub messaging
* **pluginbase-games** - Minigame state framework

## Version Compatibility

**Supported:** All major versions between Minecraft 1.8.8 - 1.21.10.

## Quick Start

See [Getting Started](getting-started.md) for installation instructions and your first plugin. This documentation focuses on example-driven guides for core features and optional modules.
