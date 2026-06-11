import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import java.net.HttpURLConnection
import java.net.URI
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

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

@DisableCachingByDefault(because = "Publishes artifacts to GitHub Releases.")
abstract class PublishGithubReleaseTask : DefaultTask() {
    @get:Internal
    abstract val token: Property<String>

    @get:Input
    abstract val repository: Property<String>

    @get:Input
    abstract val releaseTitle: Property<String>

    @get:Input
    abstract val releaseTag: Property<String>

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val changelogFile: RegularFileProperty

    @get:Input
    abstract val jarPaths: ListProperty<String>

    @TaskAction
    fun publish() {
        val tokenValue = token.orNull
            ?: throw GradleException("GITHUB_TOKEN is not set.")
        val repositoryValue = repository.get().also {
            require(it.matches(Regex("""[^/\s]+/[^/\s]+"""))) {
                "publish.github must be in the form owner/repository."
            }
        }

        val changelog = changelogFile.get().asFile.readText(StandardCharsets.UTF_8)
        val jars = jarPaths.get().map(::File).onEach { jar ->
            require(jar.isFile) { "Release jar was not found: ${jar.absolutePath}" }
        }

        fun Any?.toJsonValue() = JsonOutput.toJson(this)

        val createBody = """
            {
              "tag_name": ${releaseTag.get().toJsonValue()},
              "name": ${releaseTitle.get().toJsonValue()},
              "body": ${changelog.toJsonValue()},
              "draft": false,
              "prerelease": false
            }
        """.trimIndent().toByteArray(StandardCharsets.UTF_8)

        val releaseApi = "https://api.github.com/repos/$repositoryValue/releases"
        val (createStatus, createResponse) = githubRequest("POST", releaseApi, tokenValue, body = createBody)
        if (createStatus !in 200..299) {
            throw GradleException("Failed to create GitHub Release ($createStatus): $createResponse")
        }

        @Suppress("UNCHECKED_CAST")
        val releaseId = (JsonSlurper().parseText(createResponse) as Map<String, Any>)["id"]
            ?: throw GradleException("Could not find GitHub release id in API response.")

        jars.forEach { jar ->
            val encodedName = URLEncoder.encode(jar.name, StandardCharsets.UTF_8).replace("+", "%20")
            val uploadUri = "https://uploads.github.com/repos/$repositoryValue/releases/$releaseId/assets?name=$encodedName"
            val (uploadStatus, uploadResponse) = githubRequest(
                method = "POST",
                uri = uploadUri,
                token = tokenValue,
                contentType = "application/java-archive",
                body = jar.readBytes()
            )
            if (uploadStatus !in 200..299) {
                throw GradleException("Failed to upload ${jar.name} to GitHub Release ($uploadStatus): $uploadResponse")
            }
        }
    }

    private fun githubRequest(
        method: String,
        uri: String,
        token: String,
        contentType: String? = "application/json",
        body: ByteArray? = null
    ): Pair<Int, String> {
        val connection = (URI(uri).toURL().openConnection() as HttpURLConnection).apply {
            requestMethod = method
            setRequestProperty("Accept", "application/vnd.github+json")
            setRequestProperty("Authorization", "Bearer $token")
            setRequestProperty("X-GitHub-Api-Version", "2022-11-28")
            contentType?.let { setRequestProperty("Content-Type", it) }
            body?.let {
                doOutput = true
                outputStream.use { os -> os.write(it) }
            }
        }

        val status = connection.responseCode
        val response = runCatching {
            val stream = if (status in 200..299) connection.inputStream else connection.errorStream
            stream?.bufferedReader(StandardCharsets.UTF_8)?.use { it.readText() }.orEmpty()
        }.getOrDefault("")
        connection.disconnect()
        return status to response
    }
}

val githubToken = providers.environmentVariable("GITHUB_TOKEN")
val githubRepository = property("publish.github").toString()
val githubModId = property("mod.id").toString()
val githubModName = property("mod.name").toString()
val githubModVersion = property("mod.version").toString()
val githubReleaseTitle = "$githubModName $githubModVersion"
val githubReleaseTag = "v$githubModVersion"
val githubChangelogFile = layout.projectDirectory.file("CHANGELOG.md")
val githubReleaseJarFiles = releaseVersions.map { version ->
    layout.buildDirectory.file("libs/$githubModVersion/remapped/$githubModId-v$githubModVersion-mc$version.jar")
        .get()
        .asFile
        .absolutePath
}

tasks.register<PublishGithubReleaseTask>("publishGithubRelease") {
    group       = "publishing"
    description = "Build release jars and publish them to a GitHub Release."
    dependsOn("buildReleaseRemapped")

    token.set(githubToken)
    repository.set(githubRepository)
    releaseTitle.set(githubReleaseTitle)
    releaseTag.set(githubReleaseTag)
    changelogFile.set(githubChangelogFile)
    jarPaths.set(githubReleaseJarFiles)
}

gradle.projectsEvaluated {
    releaseVersions.zipWithNext().forEach { (prev, next) ->
        project(":$next").tasks.named("publishModrinth") {
            mustRunAfter(":$prev:publishModrinth")
        }
    }
}
*/
