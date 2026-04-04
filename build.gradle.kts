// Root project — all shared configuration lives in buildSrc/pluginbase-conventions

tasks.register<Exec>("setupGitHooks") {
    group = "setup"
    description = "Configure git to use the .githooks directory"
    commandLine("git", "config", "core.hooksPath", ".githooks")
}

tasks.register("fix") {
    group = "verification"
    description = "Auto-fix formatting issues"
    dependsOn(subprojects.map { ":${it.name}:spotlessApply" })
}
