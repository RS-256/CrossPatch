plugins {
    id("dev.kikugie.stonecutter")
    id("net.fabricmc.fabric-loom") version "1.16-SNAPSHOT" apply false
    id("net.fabricmc.fabric-loom-remap") version "1.16-SNAPSHOT" apply false
    // id("me.modmuss50.mod-publish-plugin") version "1.1.0" apply false  // uncomment to enable publishing
}

stonecutter active "26.1.2"

// ---------------------------------------------------------------
// Stonecutter parameters - available in every versioned subproject
// See https://stonecutter.kikugie.dev/wiki/config/params
// ---------------------------------------------------------------
stonecutter parameters {
    swaps["mod_version"] = "\"${property("mod.version")}\";"
    swaps["minecraft"]   = "\"${node.metadata.version}\";"
    constants["release"] = property("mod.id") != "template"
    dependencies["fapi"] = node.project.property("deps.fabric_api") as String

    // As Mojang has carried out a simple refactoring, this can be resolved simply by replacing the text
    // replacements {
    //     string(current.parsed >= "1.21.11") {
    //         replace("ResourceLocation", "Identifier")
    //     }
    // }
}

// ---------------------------------------------------------------
// Convenience run tasks — delegates to the currently active version
// ---------------------------------------------------------------
tasks.register("runClientCurrentVersion") {
    group       = "run"
    description = "Runs the client for the active stonecutter version."
    dependsOn(project(":${sc.current?.version}").tasks.named("runClient"))
}

tasks.register("runServerCurrentVersion") {
    group       = "run"
    description = "Runs the server for the active stonecutter version."
    dependsOn(project(":${sc.current?.version}").tasks.named("runServer"))
}

// ---------------------------------------------------------------
// Release version list — versions that actually get published.
// Each entry maps to one Modrinth upload (or CurseForge).
// ---------------------------------------------------------------
val releaseVersions = listOf(
    "1.21.11",
    "26.1.2"
)

extra["publish.changelogReleaseVersion"] = releaseVersions.last()

tasks.register("buildReleaseRemapped") {
    group       = "build"
    description = "Build remapped jars only for the release versions."
    dependsOn(releaseVersions.map { v -> ":$v:buildAndCollectRemapped" })
}

// ---------------------------------------------------------------
// Publisher - uncomment the blocks below after enabling the
// mod-publish-plugin above and setting publish.modrinth /
// publish.curseforge in gradle.properties.
// ---------------------------------------------------------------
/*
stonecutter tasks {
    order("publishModrinth")
    order("publishCurseforge")
}

tasks.register("publishAllToModrinthRelease") {
    group       = "publishing"
    description = "Publish all release versions to Modrinth in order."
    dependsOn(releaseVersions.map { ":$it:publishModrinth" })
}

gradle.projectsEvaluated {
    releaseVersions.zipWithNext().forEach { (prev, next) ->
        project(":$next").tasks.named("publishModrinth") {
            mustRunAfter(":$prev:publishModrinth")
        }
    }
}
*/
