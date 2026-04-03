plugins {
    id("pluginbase-conventions")
}

dependencies {
    compileOnly(project(":pluginbase-core"))

    implementation(libs.jedis)
    implementation(libs.commons.pool2)
}

tasks.shadowJar {
    dependencies {
        include(dependency("redis.clients:jedis"))
        include(dependency("org.apache.commons:commons-pool2"))
    }

    relocate("redis.clients.jedis", "dev.demeng.pluginbase.redis.lib.jedis")
    relocate("org.apache.commons.pool", "dev.demeng.pluginbase.redis.lib.commonspool")
}
