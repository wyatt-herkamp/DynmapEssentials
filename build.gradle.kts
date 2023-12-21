import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

/*
 * This file was generated by the Gradle 'init' task.
 */

plugins {
    `java-library`
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "8.1.1"
    kotlin("jvm")
    id("net.minecrell.plugin-yml.bukkit") version "0.6.0"
    id("idea")
}

repositories {
    mavenLocal()
    maven {
        url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    }
    maven {
        url = uri("https://ci.ender.zone/plugin/repository/everything/")
    }
    maven {
        url = uri("https://repo.mikeprimm.com/")
    }
    maven {
        url = uri("https://repo.codemc.org/repository/maven-public")
    }
    maven {
        url = uri("https://repo.maven.apache.org/maven2/")
    }
    mavenCentral()
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.16.4-R0.1-SNAPSHOT")
    compileOnly("net.ess3:EssentialsX:2.18.0")
    compileOnly("us.dynmap:dynmap-api:3.6")
    compileOnly("us.dynmap:DynmapCoreAPI:3.6")
    implementation("org.bstats:bstats-bukkit:3.0.2")
    implementation(kotlin("stdlib-jdk8"))
}

group = "dev.kingtux"
version = "1.3"
description = "DynmapEssentials"
tasks.withType<JavaCompile>() {
    options.encoding = "UTF-8"
}
kotlin {
    jvmToolchain(11)
}
bukkit {
    main = "dev.kingtux.dynmapessentials.DynmapEssentials"
    apiVersion = "1.16"
    authors = listOf("KingTux")
    depend = listOf("Essentials", "dynmap")
    description = "Adds support for Essentials Warps inside Dynmap"
    website = "https://github.com/wyatt-herkamp/DynmapEssentials"
}
tasks {
    named<ShadowJar>("shadowJar") {
        relocate("org.bstats", "dev.kingtux.dynmapessentials.bstats")
    }
    build {
        dependsOn(shadowJar)
    }
    jar{
        enabled = false
    }
}
idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}