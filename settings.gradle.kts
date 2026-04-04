plugins {
    id("com.gradleup.nmcp.settings") version "1.4.4"
}

rootProject.name = "pluginbase"

nmcpSettings {
    centralPortal {
        username = providers.gradleProperty("centralPortalUsername")
        password = providers.gradleProperty("centralPortalPassword")
        publishingType = "AUTOMATIC"
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
        maven("https://jitpack.io")
        maven("https://repo.codemc.org/repository/maven-public")
        maven("https://libraries.minecraft.net/")
    }
}

include(
    "pluginbase-core",
    "pluginbase-sql",
    "pluginbase-mongo",
    "pluginbase-games",
    "pluginbase-redis"
)
