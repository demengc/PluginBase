plugins {
    id("pluginbase-conventions")
}

dependencies {
    compileOnly(libs.authlib)

    implementation(libs.bundles.lamp)
    implementation(libs.xseries)
    implementation(libs.adventure.api) {
        exclude(group = "net.kyori", module = "adventure-bom")
    }
    implementation(libs.adventure.platform.bukkit) {
        exclude(group = "net.kyori", module = "adventure-bom")
        exclude(group = "net.kyori", module = "adventure-api")
    }
    implementation(libs.adventure.text.minimessage)
    implementation(libs.expiringmap)
}

tasks.shadowJar {
    relocate("revxrsal.commands", "dev.demeng.pluginbase.lib.lamp")
    relocate("com.cryptomorin.xseries", "dev.demeng.pluginbase.lib.xseries")
    relocate("net.jodah.expiringmap", "dev.demeng.pluginbase.lib.expiringmap")
    relocate("net.kyori.adventure", "dev.demeng.pluginbase.lib.adventure")
    relocate("net.kyori.examination", "dev.demeng.pluginbase.lib.examination")
    relocate("net.kyori.option", "dev.demeng.pluginbase.lib.option")
    relocate("com.google.auto.service", "dev.demeng.pluginbase.lib.autoservice")
    relocate("org.jspecify.annotations", "dev.demeng.pluginbase.lib.jspecify")

    exclude("com/cryptomorin/xseries/SkullCacheListener*")
    exclude("com/cryptomorin/xseries/NoteBlockMusic*")
}
