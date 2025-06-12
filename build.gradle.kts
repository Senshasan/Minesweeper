plugins {
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

// Create a separate configuration for PlantUML to avoid including it in the main JAR
configurations {
    create("plantuml")
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    // PlantUML dependency for diagram generation (separate configuration)
    "plantuml"("net.sourceforge.plantuml:plantuml:1.2024.3")
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
        // Clean the native directory if it exists
        val nativeDir = file("build/native")
        if (nativeDir.exists()) {
            nativeDir.deleteRecursively()
        }

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

// PlantUML diagram generation task
tasks.register("generateDiagrams") {
    group = "documentation"
    description = "Generates PlantUML class diagrams from Java source code"

    doLast {
        // Create diagrams directory if it doesn't exist
        val diagramsDir = file("diagrams")
        if (!diagramsDir.exists()) {
            diagramsDir.mkdirs()
        }

        // Create a basic PlantUML file by parsing Java classes
        val plantUmlContent = StringBuilder()
        plantUmlContent.appendLine("@startuml")
        plantUmlContent.appendLine("!theme plain")
        plantUmlContent.appendLine("skinparam classAttributeIconSize 0")
        plantUmlContent.appendLine("")

        // Parse Java files and extract class information
        fileTree("src/main/java").matching { include("**/*.java") }.forEach { javaFile ->
            val content = javaFile.readText()
            val className = javaFile.nameWithoutExtension

            // Extract class declaration
            val isInterface = content.contains("interface $className")
            val isAbstract = content.contains("abstract class $className")
            val extendsMatch = Regex("extends\\s+(\\w+)").find(content)
            val implementsMatch = Regex("implements\\s+([\\w,\\s]+)").find(content)

            // Add class declaration
            when {
                isInterface -> plantUmlContent.appendLine("interface $className {")
                isAbstract -> plantUmlContent.appendLine("abstract class $className {")
                else -> plantUmlContent.appendLine("class $className {")
            }

            // Extract fields (simplified)
            val fieldPattern = Regex("\\s+(private|public|protected)\\s+([\\w<>\\[\\]]+)\\s+(\\w+)")
            fieldPattern.findAll(content).forEach { match ->
                val visibility = when(match.groupValues[1]) {
                    "private" -> "-"
                    "public" -> "+"
                    "protected" -> "#"
                    else -> "~"
                }
                plantUmlContent.appendLine("  $visibility${match.groupValues[3]} : ${match.groupValues[2]}")
            }

            // Extract methods (simplified)
            val methodPattern = Regex("\\s+(private|public|protected)\\s+([\\w<>\\[\\]]+)\\s+(\\w+)\\s*\\(([^)]*)\\)")
            methodPattern.findAll(content).forEach { match ->
                val visibility = when(match.groupValues[1]) {
                    "private" -> "-"
                    "public" -> "+"
                    "protected" -> "#"
                    else -> "~"
                }
                val methodName = match.groupValues[3]
                val returnType = match.groupValues[2]
                if (methodName != className) { // Skip constructors for now
                    plantUmlContent.appendLine("  $visibility$methodName() : $returnType")
                }
            }

            plantUmlContent.appendLine("}")
            plantUmlContent.appendLine("")

            // Add relationships
            extendsMatch?.let {
                plantUmlContent.appendLine("${it.groupValues[1]} <|-- $className")
            }
            implementsMatch?.let {
                val interfaces = it.groupValues[1].split(",").map { it.trim() }
                interfaces.forEach { interfaceName ->
                    plantUmlContent.appendLine("$interfaceName <|.. $className")
                }
            }
        }

        plantUmlContent.appendLine("@enduml")

        // Write PlantUML file
        val pumlFile = file("diagrams/classes.puml")
        pumlFile.writeText(plantUmlContent.toString())

        // Generate PNG from PlantUML
        exec {
            commandLine(
                "java",
                "-jar",
                configurations.getByName("plantuml").singleFile.absolutePath,
                "-tpng",
                "diagrams/classes.puml"
            )
        }

        println("Class diagrams generated:")
        println("- diagrams/classes.puml (PlantUML source)")
        println("- diagrams/classes.png (PNG image)")
    }
}