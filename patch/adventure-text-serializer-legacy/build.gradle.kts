import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.github.retrooper.excludeAdventure

plugins {
    packetevents.`patching-conventions`
}

dependencies {
    compileOnly(project(":patch:common"))

    sequenceOf(
        libs.adventure.text.serializer.legacy,
    ).forEach {
        compileOnlyApi(it)
        runtimeOnly(it) {
            exclude(module = "adventure-text-serializer-commons")
            excludeAdventure()
        }
    }
}

tasks.withType<ShadowJar> {
    exclude("META-INF/services/**")
}
