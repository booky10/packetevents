import com.github.jengelman.gradle.plugins.shadow.internal.DependencyFilter
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    com.gradleup.shadow
}

tasks {
    shadowJar {
        destinationDirectory = rootProject.layout.buildDirectory.dir("libs")
        archiveFileName = "packetevents-${project.name}-${rootProject.ext["versionNoHash"]}.jar"
        archiveClassifier = null

        // don't relocate parent package, would relocate class files from parent adventure dependency
        sequenceOf("legacy", "json", "gson", "commons").forEach {
            relocate(
                "net.kyori.adventure.text.serializer.$it",
                "io.github.retrooper.packetevents.adventure.serializer.$it"
            )
        }
        relocate("net.kyori.option", "io.github.retrooper.packetevents.adventure.option")
        relocate("org.bstats", "io.github.retrooper.packetevents.bstats")

        mergeServiceFiles()
    }

    register<ShadowJar>("shadowNoAdventure") {
        group = rootProject.name
        description = "Create a combined JAR of project and runtime dependencies without Adventure dependencies."
        archiveFileName = "packetevents-${project.name}-no-adv-${rootProject.ext["versionNoHash"]}.jar"
        archiveClassifier = null

        val shadowJar = shadowJar.get()
        val sourceSets = project.extensions.getByType<SourceSetContainer>()

        manifest.from(shadowJar.manifest)

        from(sourceSets.main.get().output)
        configurations = shadowJar.configurations

        relocate("net.kyori.adventure.text.serializer", "io.github.retrooper.packetevents.adventure.serializer")
        relocate("net.kyori.option", "io.github.retrooper.packetevents.adventure.option")
        relocate("org.bstats", "io.github.retrooper.packetevents.bstats")

        dependencies {
            exclude(dependency("net.kyori:adventure-api:.*"))
            exclude(dependency("net.kyori:adventure-key:.*"))
            exclude(dependency("net.kyori:adventure-nbt:.*"))
            exclude(dependency("net.kyori:examination-api:.*"))
            exclude(dependency("net.kyori:examination-string:.*"))
            exclude(dependency("com.google.code.gson:gson:.*"))
            exclude("META-INF/INDEX.LIST", "META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA", "module-info.class")
        }

        mergeServiceFiles()
    }

    assemble {
        dependsOn(shadowJar)
    }

    if (project.properties.contains("no-adv")) {
        assemble {
            dependsOn("shadowNoAdventure")
        }
    }
}

fun DependencyFilter.exclude(dependency: ResolvedDependency) {
    exclude(dependency("${dependency.moduleGroup}:${dependency.moduleName}:.*"))
    dependency.children.forEach {
        exclude(it)
    }
}
