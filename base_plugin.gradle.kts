@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    id("java-gradle-plugin")
    alias(libs.plugins.org.jetbrains.kotlin.jvm)
    id("maven-publish")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}



dependencies{

    implementation(project(":configgen-core"))
    implementation(libs.poet.java)


}
//com.location.configGen

val ARTIFACT_ID = "com.location.configGen-java"

gradlePlugin {
    plugins {
        create("com.location.configGen-java") {
            id = ARTIFACT_ID
            implementationClass = "com.location.configgen.core.ConfigGenPlugin"
        }
    }
}
tasks.register("publishSourcesJar", Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.getByName("main").allSource)
}

tasks.register("publishJavadocsJar", Jar::class) {
    dependsOn("publishSourcesJar")
    archiveClassifier.set("javadoc")
}

afterEvaluate {
    publishing{
        publications {
            create<MavenPublication>("mavenJava") {
                from(components["java"])
                groupId = "com.location.configGen-java"
                artifactId = "$ARTIFACT_ID.gradle.plugin"

                version = "1.0.1"
                artifact(tasks.getByName("publishSourcesJar"))
                artifact(tasks.getByName("publishJavadocsJar"))
            }
        }
        repositories {
            maven {
                url = uri("${rootDir.absolutePath}/localRepo")
            }
        }
    }

}