plugins {
    `java-library`
    checkstyle
}

configure<CheckstyleExtension> {
    configFile = rootProject.file("buildSrc/src/main/resources/checkstyle.xml")
    // TODO version catalog
    toolVersion = "13.4.2"
}

// checkstyle needs at least java 11
tasks.withType<Checkstyle> {
    javaLauncher = javaToolchains.launcherFor {
        reports {
            xml.required = false
            html.required = true
        }
        languageVersion = JavaLanguageVersion.of(21)
    }
}
