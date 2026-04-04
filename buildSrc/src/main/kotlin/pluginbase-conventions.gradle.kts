import net.ltgt.gradle.errorprone.errorprone

plugins {
    `java-library`
    `maven-publish`
    id("com.gradleup.shadow")
    id("com.diffplug.spotless")
    id("net.ltgt.errorprone")
}

group = "dev.demeng"
version = "1.37.0-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
    withSourcesJar()
    withJavadocJar()
}

spotless {
    java {
        googleJavaFormat()
        targetExclude("build/**")
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.errorprone {
        disableWarningsInGeneratedCode.set(true)
    }
}

tasks.javadoc {
    (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")
}

val libs = the<VersionCatalogsExtension>().named("libs")

dependencies {
    "compileOnly"(libs.findLibrary("spigot-api").get())
    "compileOnly"(libs.findLibrary("annotations").get())
    "compileOnly"(libs.findLibrary("jsr305").get())
    "compileOnly"(libs.findLibrary("lombok").get())
    "annotationProcessor"(libs.findLibrary("lombok").get())
    "errorprone"(libs.findLibrary("errorprone-core").get())

    "testImplementation"(libs.findLibrary("junit-jupiter").get())
    "testRuntimeOnly"(libs.findLibrary("junit-platform-launcher").get())
    "testImplementation"(libs.findLibrary("assertj-core").get())
    "testCompileOnly"(libs.findLibrary("lombok").get())
    "testAnnotationProcessor"(libs.findLibrary("lombok").get())
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

tasks.shadowJar {
    archiveClassifier.set("")
    mergeServiceFiles()
}

tasks.named<Jar>("jar") {
    archiveClassifier.set("slim")
}

tasks.named("assemble") {
    dependsOn(tasks.named("shadowJar"))
}

publishing {
    publications {
        create<MavenPublication>("shadow") {
            artifact(tasks.shadowJar)
            artifact(tasks.named("sourcesJar"))
            artifact(tasks.named("javadocJar"))
        }
    }
}
