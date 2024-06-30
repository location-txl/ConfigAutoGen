import org.jetbrains.kotlin.fir.declarations.builder.buildConstructor

@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    id("configgen.java")
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
        buildConfigField("int", "a", "1")
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
    flavorDimensions("server", "product")
    productFlavors{
        create("free") {
            dimension = "product"
        }
        create("pro") {
            dimension = "product"

        }
        create("serverTest") {
            dimension = "server"
        }
        create("serverRelease") {
            dimension = "server"
        }
    }
}
configWeaver {
    debugLog = true

    java{
        nullSafe = true
    }

//    java {
//        nullSafe = true
//    }

    customObject {

        create("HostConfig2"){
            addProperty("url", "http://localhost:3000")
            addListProperty("list"){
                add(1)
                add(2)
                add(3)
                add(4)
                add(5)
            }
            addListObject("apis"){
                add {
                    addProperty("path", "getName")
                    addProperty("name", "1")
                    addProperty("id", 1)
                }
                add {
                    addProperty("path", "getAge")
                    addProperty("name", "2")
                    addProperty("id", 1719734598204)
                }
                add {
                    addProperty("path", "getAddress")
                    addProperty("name", "3")
                }
            }
            addObject("timeout"){
                addProperty("read", 10000)
                addProperty("connect", 1)
            }

            withFlavor("free"){
                addProperty("url", "www.debug.com")
                addProperty("id", 1)
            }

        }


        create("SampleConfig") {
                addProperty("value_string", "hello configWeaver")
                addProperty("value_int", 1)
                addProperty("value_float", 1.1f)
                addProperty("value_boolean", true)
                addObject("sample_sub_config") {
                    addProperty("sub_value_string", "hello sub configWeaver")
                }
                withFlavor("free") {
                    addProperty("value_string", "use product free hello configWeaver")
                }
                addListProperty("testList") {
                    add(1)
                    add(2)
                }
                addListObject("testListObject") {
                    add {
                        addProperty("test_child_1", "hello configWeaver")
                        addProperty("test_child_2", 1)
                    }
                    add {
                        addProperty("test_child_2", 3)
                    }
                }
            }

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