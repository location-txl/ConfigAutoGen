@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
//    id("java-gradle-plugin")
    id("java-library")
    alias(libs.plugins.org.jetbrains.kotlin.jvm)
    id("maven-publish")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}



dependencies{
    implementation(gradleApi())
    implementation(libs.json.simple)
    implementation(libs.agp.api)
    implementation(libs.agp.tool)
    implementation(libs.poet.java)
    implementation(libs.poet.kotlin)

}
//com.location.configGen

val ARTIFACT_ID = "configGen-core"


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
                groupId = "com.location.configGen"
                artifactId = ARTIFACT_ID

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