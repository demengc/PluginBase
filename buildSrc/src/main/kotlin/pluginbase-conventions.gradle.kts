import net.ltgt.gradle.errorprone.errorprone

plugins {
    `java-library`
    `maven-publish`
    signing
    id("com.gradleup.shadow")
    id("com.diffplug.spotless")
    id("net.ltgt.errorprone")
}

group = "dev.demeng.pluginbase"
version = findProperty("releaseVersion") as String? ?: "0.0.0-SNAPSHOT"

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
    "testImplementation"(libs.findLibrary("mockito-core").get())
    "testImplementation"(libs.findLibrary("guava").get())
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
            pom {
                name.set(project.name)
                description.set(provider { project.description ?: project.name })
                url.set("https://github.com/demengc/PluginBase")
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                developers {
                    developer {
                        id.set("demengc")
                        name.set("Demeng")
                        email.set("hi@demeng.dev")
                        url.set("https://github.com/demengc")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/demengc/PluginBase.git")
                    developerConnection.set("scm:git:ssh://github.com/demengc/PluginBase.git")
                    url.set("https://github.com/demengc/PluginBase")
                }
            }
        }
    }
}

signing {
    val signingKey = findProperty("signingInMemoryKey") as String?
    val signingPassword = findProperty("signingInMemoryKeyPassword") as String?
    if (signingKey != null) {
        useInMemoryPgpKeys(signingKey, signingPassword)
    }
    sign(publishing.publications["shadow"])
}

tasks.withType<Sign>().configureEach {
    isRequired = !version.toString().endsWith("-SNAPSHOT")
}
