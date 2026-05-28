plugins {
    `java-library`
    checkstyle
}

configure<CheckstyleExtension> {
    configFile = rootProject.file("buildSrc/src/main/resources/checkstyle.xml")
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

dependencies {
    // TODO version catalog
    checkstyle("com.puppycrawl.tools:checkstyle:13.4.2")
}
