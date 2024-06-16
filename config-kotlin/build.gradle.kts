@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    id("java-gradle-plugin")
    alias(libs.plugins.org.jetbrains.kotlin.jvm)
}





dependencies {
    implementation(libs.poet.kotlin)
}






















