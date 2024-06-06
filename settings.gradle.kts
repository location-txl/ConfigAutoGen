import java.net.URI

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven{
            url = uri("${rootProject.projectDir.absolutePath}/configmerge/build/repo")
        }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven{
            url = uri("${rootProject.projectDir.absolutePath}/configmerge/build/repo")
        }
    }
}

rootProject.name = "ConfigAutoGen"
include(":app")
include(":configmerge")
