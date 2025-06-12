plugins {
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

// JAR configuration (your original working setup)
tasks.jar {
    manifest {
        attributes(
            "Main-Class" to "Main"
        )
    }

    // Make it a "fat JAR" with everything included
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    // Give it a nice name
    archiveBaseName.set("Minesweeper")
    archiveVersion.set("1.0")
}

// Custom task to create native executable using jpackage directly
tasks.register("createExe") {
    group = "distribution"
    description = "Creates a native executable using jpackage"
    dependsOn("jar")

    doLast {
        exec {
            commandLine(
                "jpackage",
                "--input", "build/libs",
                "--name", "Minesweeper",
                "--main-jar", "Minesweeper-1.0.jar",
                "--main-class", "Main",
                "--type", "app-image",
                "--dest", "build/native"
            )
        }
    }
}