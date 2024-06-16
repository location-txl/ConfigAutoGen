include(":config-core")
include(":config-java")
include(":config-kotlin")


pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven{
            url = uri("${rootDir.absolutePath}/localRepo")
        }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven{
            url = uri("${rootDir.absolutePath}/localRepo")
        }
    }
}

rootProject.name = "ConfigWeaver"
include(":app")
