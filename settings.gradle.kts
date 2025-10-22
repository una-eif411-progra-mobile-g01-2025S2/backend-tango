pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS) // o FAIL_ON_PROJECT_REPOS
    repositories {
        mavenCentral()
        // (opcional) maven("https://repo.spring.io/release")
    }
}

rootProject.name = "backend-tango"
