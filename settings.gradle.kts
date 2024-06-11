import java.net.URI

include(":configgen-java")


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

rootProject.name = "ConfigAutoGen"
include(":app")
include(":configgen-core")
