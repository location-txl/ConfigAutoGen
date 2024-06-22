@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    id("configgen.kotlin")
}

android {
    namespace = "com.location.configgen"
    compileSdk = 33

    defaultConfig {
        applicationId = "com.location.configgen"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    flavorDimensions("product", "device")
    productFlavors{
        create("v1") {
            dimension = "product"
        }
        create("v2") {
            dimension = "product"

        }
        create("v3") {
            dimension = "product"
        }
        create("pad"){
            dimension = "device"
        }
        create("phone") {
            dimension = "device"
        }
    }
}
configWeaver {
    customObject {
        create("ConfigTest") {
            addProperty("id", 1)
            addProperty("u_Id", 2.1f)
            addProperty("name", "tom")
            addListProperty("uIdList"){
                add(1)
                add(2)
            }
            addListObject("config2"){
                add {
                    addProperty("ab", 1)
                    addObject("user"){
                        addProperty("name", "123")
                    }
                }
                add {
                    addProperty("ab", 1)
                    addObject("user"){
                        addProperty("id", 1)
                    }
                }
            }
            addObject("subObj"){
                addProperty("hello2", "name")
            }

        }

        create("LocalManager"){
            addProperty("helloname","txla")
            addObject("subObj"){
                addProperty("hello2", "name")
                addObject("sub2"){
                    addProperty("nihao",123)
                }
            }
            withFlavor("debug"){
                addProperty("iddl2",1)
                addObject("debugConfig"){
                    addProperty("logLevel", 1)
                }
            }
        }
    }
}





dependencies {
    implementation(libs.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
}