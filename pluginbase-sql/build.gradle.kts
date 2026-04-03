plugins {
    id("pluginbase-conventions")
}

dependencies {
    compileOnly(project(":pluginbase-core"))

    implementation(libs.hikaricp) {
        exclude(group = "org.slf4j", module = "slf4j-api")
    }
    implementation(libs.sqlstreams)
}

tasks.shadowJar {
    relocate("com.zaxxer.hikari", "dev.demeng.pluginbase.sql.lib.hikari")
    relocate("be.bendem.sqlstreams", "dev.demeng.pluginbase.sql.lib.sqlstreams")
}
