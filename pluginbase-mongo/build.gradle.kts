plugins {
    id("pluginbase-conventions")
}

dependencies {
    compileOnly(project(":pluginbase-core"))

    implementation(libs.mongodb.driver.sync)
}

tasks.shadowJar {
    dependencies {
        include(dependency("org.mongodb:mongodb-driver-sync"))
        include(dependency("org.mongodb:mongodb-driver-core"))
        include(dependency("org.mongodb:bson"))
    }

    relocate("com.mongodb", "dev.demeng.pluginbase.mongo.lib.driver")
    relocate("org.bson", "dev.demeng.pluginbase.mongo.lib.bson")
}
