plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://plugins.gradle.org/m2/")
    }
}

dependencies {
    implementation("org.jetbrains.intellij.platform:intellij-platform-gradle-plugin:2.7.0")
    implementation("org.jetbrains.intellij.plugins:gradle-changelog-plugin:2.2.1")
    implementation("com.dorongold.plugins:task-tree:4.0.1") // provides `taskTree` task (e.g. `./gradlew build taskTree`; docs: https://github.com/dorongold/gradle-task-tree)
}

