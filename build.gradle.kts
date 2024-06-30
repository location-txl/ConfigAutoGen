import com.vanniktech.maven.publish.MavenPublishBaseExtension
import org.gradle.api.publish.PublishingExtension
buildscript {
    repositories {
        maven{
            url = uri("${rootDir.absolutePath}/localRepo")
        }
        mavenCentral()

    }
    dependencies {
        classpath(libs.codeGen.java)
//        classpath(libs.codeGen.kotlin)
    }

}

@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.kotlinAndroid) apply false
    alias(libs.plugins.mavenPublish) apply false
    alias(libs.plugins.org.jetbrains.kotlin.jvm) apply false
//    alias(libs.plugins.config.merge) apply false

}

subprojects {
    println(name)

    afterEvaluate {

        if (plugins.hasPlugin("java-gradle-plugin")) {
            apply(plugin = libs.plugins.mavenPublish.get().pluginId)
            val depProjects = listOf(project(":config-core"))
            pluginManager.withPlugin("java") {
                extensions.configure<JavaPluginExtension> {
                    withSourcesJar()
                    sourceCompatibility = JavaVersion.VERSION_11
                    targetCompatibility = JavaVersion.VERSION_11

                    tasks.named<Jar>("sourcesJar") {
                        archiveClassifier.set("sources")
                        from(sourceSets.getByName("main").allSource)
                        depProjects.forEach { depProject ->
                            from(depProject.the<JavaPluginExtension>().sourceSets.getByName("main").allSource)
                        }
                    }

                    tasks.register("publishDocsJar", Jar::class) {
                        dependsOn("publishSourcesJar")
                        archiveClassifier.set("javadoc")
                    }

                    tasks.named<Jar>("jar") {
                        doFirst {
                            depProjects.forEach {
                                from(
                                    it.layout.buildDirectory.dir("classes/kotlin/main").get().asFile
                                )
                            }
                        }
                    }

                }
            }

            apply(plugin = "maven-publish")
            println("has")
            dependencies {
                "implementation"(libs.bundles.codeGenCore)
                "compileOnly"(libs.bundles.codeGenCompile)
                "testImplementation"(libs.junit)
                "testImplementation"(libs.truth)
                depProjects.forEach {
                    "compileOnly"(it)
                }
            }
            val pluginId = project.properties["plugin.id"] as? String
                ?: error("project gradle.properties must config plugin.id")
            val pluginClass = project.properties["plugin.class"] as? String
                ?: error("project gradle.properties must config plugin.class")

            pluginManager.withPlugin("java-gradle-plugin") {
                extensions.configure<GradlePluginDevelopmentExtension> {
                    plugins {
                        create("com.location.configGen-kotlin") {
                            id = pluginId
                            implementationClass = pluginClass
                            version = "1.0.9"
                        }
                    }
                }
            }



            configure<PublishingExtension> {
                publications {
                    repositories {
                        mavenLocal {
                            url = uri("${rootDir.absolutePath}/localRepo")
                        }
                    }
                }

            }

            plugins.withId(libs.plugins.mavenPublish.get().pluginId) {
                val publishingExtension = extensions.getByType(PublishingExtension::class.java)
                configure<MavenPublishBaseExtension> {

                }

            }

        }
    }

}

true // Needed to make the Suppress annotation work for the plugins block