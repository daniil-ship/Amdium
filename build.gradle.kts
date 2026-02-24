import net.minecraftforge.gradle.userdev.UserDevExtension
import org.spongepowered.asm.gradle.plugins.MixinExtension

buildscript {
    repositories {
        maven { url = uri("https://maven.minecraftforge.net/") }
        mavenCentral()
    }
}

plugins {
    eclipse
    idea
    `maven-publish`
    id("net.minecraftforge.gradle") version "[6.0,6.2)"
    id("org.spongepowered.mixin") version "0.7.+"
}

// ============================================
// Читаем свойства из gradle.properties
// ============================================
val modId: String by project
val modName: String by project
val modVersion: String by project
val modGroup: String by project
val minecraftVersion: String by project
val forgeVersion: String by project
val mappingChannel: String by project
val mappingVersion: String by project

// Превращаем snake_case из gradle.properties
val mcVersion = project.property("minecraft_version") as String
val frgVersion = project.property("forge_version") as String
val mapChannel = project.property("mapping_channel") as String
val mapVersion = project.property("mapping_version") as String
val mVersion = project.property("mod_version") as String
val mId = project.property("mod_id") as String
val mName = project.property("mod_name") as String
val mGroup = project.property("mod_group") as String

version = mVersion
group = mGroup

base {
    archivesName.set(mId)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

// ============================================
// Minecraft / Forge настройка
// ============================================
configure<UserDevExtension> {
    mappings(mapChannel, mapVersion)

    copyIdeResources = true

    runs {
        create("client") {
            workingDirectory(project.file("run"))

            property("forge.logging.markers", "REGISTRIES")
            property("forge.logging.console.level", "debug")

            // Mixin
            property("mixin.env.remapRefMap", "true")
            property("mixin.env.refMapRemappingFile",
                "${projectDir}/build/createSrgToMcp/output.srg")
            arg("-mixin.config=amdium.mixins.json")

            mods {
                create(mId) {
                    source(sourceSets.main.get())
                }
            }
        }

        create("server") {
            workingDirectory(project.file("run"))

            property("forge.logging.markers", "REGISTRIES")
            property("forge.logging.console.level", "debug")

            mods {
                create(mId) {
                    source(sourceSets.main.get())
                }
            }
        }

        create("data") {
            workingDirectory(project.file("run"))

            property("forge.logging.markers", "REGISTRIES")
            property("forge.logging.console.level", "debug")

            args(
                "--mod", mId,
                "--all",
                "--output", file("src/generated/resources/").absolutePath,
                "--existing", file("src/main/resources/").absolutePath
            )

            mods {
                create(mId) {
                    source(sourceSets.main.get())
                }
            }
        }
    }
}

// ============================================
// Mixin настройка
// ============================================
configure<MixinExtension> {
    add(sourceSets.main.get(), "amdium.refmap.json")
    config("amdium.mixins.json")
}

// ============================================
// Репозитории
// ============================================
repositories {
    maven {
        name = "MinecraftForge"
        url = uri("https://maven.minecraftforge.net/")
    }
    mavenCentral()
}

// ============================================
// Зависимости
// ============================================
dependencies {
    // Forge
    "minecraft"("net.minecraftforge:forge:${mcVersion}-${frgVersion}")

    // Mixin annotation processor
    annotationProcessor("org.spongepowered:mixin:0.8.5:processor")
}

// ============================================
// JAR настройка
// ============================================
tasks.withType<Jar> {
    manifest {
        attributes(
            "Specification-Title" to mName,
            "Specification-Vendor" to "Amdium Team",
            "Specification-Version" to "1",
            "Implementation-Title" to mName,
            "Implementation-Version" to mVersion,
            "Implementation-Vendor" to "Amdium Team",
            "MixinConfigs" to "amdium.mixins.json"
        )
    }

    // Включаем refmap в JAR
    from(sourceSets.main.get().output)

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

// ============================================
// Кодировка
// ============================================
tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

// ============================================
// Публикация (опционально)
// ============================================
publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = mId
            from(components["java"])
        }
    }
    repositories {
        maven {
            url = uri("file://${project.projectDir}/repo")
        }
    }
}

tasks.named<Jar>("jar") {
    finalizedBy("reobfJar")
}