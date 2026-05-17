plugins {
    id("pluginbase-conventions")
}

dependencies {
    compileOnly(libs.authlib)
    compileOnly(libs.adventure.api)
    compileOnly(libs.adventure.text.minimessage)
    compileOnly(libs.adventure.text.serializer.legacy)

    implementation(libs.bundles.lamp)
    implementation(libs.xseries)
    implementation(libs.expiringmap)

    testImplementation(libs.spigot.api)
    testImplementation(libs.adventure.api)
    testImplementation(libs.adventure.text.minimessage)
    testImplementation(libs.adventure.text.serializer.legacy)
}

tasks.shadowJar {
    relocate("revxrsal.commands", "dev.demeng.pluginbase.lib.lamp")
    relocate("com.cryptomorin.xseries", "dev.demeng.pluginbase.lib.xseries")
    relocate("net.jodah.expiringmap", "dev.demeng.pluginbase.lib.expiringmap")
    relocate("com.google.auto.service", "dev.demeng.pluginbase.lib.autoservice")
    relocate("org.jspecify.annotations", "dev.demeng.pluginbase.lib.jspecify")

    exclude("com/cryptomorin/xseries/SkullCacheListener*")
    exclude("com/cryptomorin/xseries/NoteBlockMusic*")
}
