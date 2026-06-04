import xyz.jpenilla.runwaterfall.task.RunWaterfall

plugins {
    packetevents.`shadow-conventions`
    packetevents.`library-conventions`
    packetevents.`publish-conventions`
    xyz.jpenilla.`run-waterfall`
}

repositories {
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly(libs.bungeecord)
    api(libs.bundles.adventure)
    implementation(libs.bstats.bungeecord)
    api(project(":netty-common"))
}

tasks.named<RunWaterfall>("runWaterfall") {
    waterfallVersion("1.21")
    runDirectory = rootProject.layout.projectDirectory.dir("run/waterfall")

    javaLauncher = project.javaToolchains.launcherFor {
        languageVersion = JavaLanguageVersion.of(21)
    }
}
