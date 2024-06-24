
@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    id("java-library")
    alias(libs.plugins.org.jetbrains.kotlin.jvm)
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}



dependencies {
    compileOnly(gradleApi())
    implementation(libs.bundles.codeGenCore)
    implementation(libs.bundles.codeGenCompile)
    implementation(libs.poet.kotlin)
    testImplementation(libs.junit)
    testImplementation(libs.truth)

}




