import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
    id("java")
    kotlin("jvm") version "1.9.0"
    id("xyz.jpenilla.run-paper") version "2.1.0"
    id("net.minecrell.plugin-yml.paper") version "0.6.0"
    id("io.papermc.paperweight.userdev") version "1.5.5"
}

group = "me.angelo4ek"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://papermc.io/repo/repository/maven-public/")
    maven("https://the-planet.fun/repo/snapshots/")
}

dependencies {
    paperweight.paperDevBundle("1.20.1-R0.1-SNAPSHOT")

    library(kotlin("stdlib"))

    library("org.spongepowered:configurate-extra-kotlin:4.1.2")

    val crystalVersion = "2.0.1"
    library("me.denarydev.crystal.paper:utils:$crystalVersion")
    library("me.denarydev.crystal.paper:serializers:$crystalVersion")
    library("me.denarydev.crystal.shared:config:$crystalVersion")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

kotlin {
    jvmToolchain(17)
}

paper {
    author = "Rafaelka UwU"

    loader = "me.rafaelka.cmdportals.loader.PluginLibrariesLoader"
    main = "me.rafaelka.cmdportals.Main"

    generateLibrariesJson = true

    foliaSupported = true

    apiVersion = "1.20"

    permissions {
        register("cmdportals.admin") {
            description = "Allows you to use all plugin commands"
            children = listOf(
                "cmdportals.wand",
                "cmdportals.reload",
                "cmdportals.create",
                "cmdportals.delete",

                "cmdportals.actions.add",
                "cmdportals.actions.remove",
                "cmdportals.actions.clear",
                "cmdportals.actions.list",

                "cmdportals.permission.set",
                "cmdportals.permission.remove",
            )
        }

        register("cmdportals.wand")
        register("cmdportals.reload")
        register("cmdportals.create")
        register("cmdportals.delete")

        register("cmdportals.actions.add")
        register("cmdportals.actions.remove")
        register("cmdportals.actions.clear")
        register("cmdportals.actions.list")

        register("cmdportals.permission.set")
        register("cmdportals.permission.remove")
    }
}

runPaper {
    folia {
        registerTask()
    }
}

tasks {
    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.compilerArgs.addAll(
            listOf(
                "-parameters",
                "-nowarn",
                "-Xlint:-unchecked",
                "-Xlint:-deprecation",
                "-Xlint:-processing"
            )
        )
        options.isFork = true
    }

    compileKotlin {
        compilerOptions.jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }

    assemble {
        dependsOn(reobfJar)
    }

    runServer {
        minecraftVersion("1.20.1")
        val file = projectDir.resolve("run/server.jar") // Check for a custom server.jar file
        if (file.exists()) serverJar(file)
    }
}
