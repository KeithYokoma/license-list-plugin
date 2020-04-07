buildscript {
    repositories {
        google()
        jcenter()
        if (System.getenv("CI") == "true") {
            mavenLocal()
        } else {
            maven {
                url = uri("https://plugins.gradle.org/m2/")
            }
        }
    }
    dependencies {
        classpath("com.android.tools.build:gradle:3.6.1")
        classpath(kotlin("gradle-plugin", version = "1.3.50")) // same to the version that kotlin-dsl uses
        classpath("io.github.jmatsu:license-list-gradle:${rootProject.file("../VERSION").readText().trim()}")
        classpath("com.cookpad.android.licensetools:license-tools-plugin:1.7.0")
    }
}
