import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    packetevents.`patching-conventions`
}

dependencies {
    api(libs.adventure.text.serializer.commons)
    api(libs.option)
}

tasks.withType<ShadowJar> {
    exclude("META-INF/services/**")
}
