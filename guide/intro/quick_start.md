# 快速开始
<br>
<Badge type="tip" text="^1.9.0" /><br>

## 安装插件
### 1. 根目录build.gradle 引入插件
::: code-group

```kotlin [build.gradle.kts]
buildscript {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        // 按需引入
        classpath(libs.codeGen.java)
        classpath(libs.codeGen.kotlin)
    }

}
```
```groovy [build.gradle]
buildscript {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        // 按需引入
        classpath(libs.codeGen.java)
        classpath(libs.codeGen.kotlin)
    }

}
```

:::
### 2. app/build.gradle 配置插件
::: code-group

```kotlin [app/build.gradle.kts]{4}
plugins {
    id("com.android.application")
    id("com.android.library")
    id("com.configweaver")
}
```
```groovy [app/build.gradle]{4}
plugins {
    id 'com.android.application'
    id 'com.android.library'
    id 'com.configweaver'
}
```
:::

## 目录结构
在 `src` 同级目录下创建 `config` 文件夹，用于存放配置json文件 

```text
ConfigWeaverSample
 ├── build.gradle.kts
 ├── settings.gradle.kts
 ├── gradle.properties
 ├── gradle
 └── app
     ├── config // [!code ++]
     │   ├──   main // [!code ++]
     │   └──   release // [!code ++]
     ├── src
     ├── build.gradle.kts
     ├── proguard-rules.pro
     └── build
```
`main` 文件夹为主配置文件夹 `debug、release`等 是变体配置文件夹
编译时会扫描 `main` 文件夹和`当前编译的变体 BuildType` 文件夹下的同名 json 文件进行合并生成对应的 Kotlin/Java 文件

假设当前有两种变体类型 默认的 BuildType
```kotlin [app/build.gradle.kts]
    flavorDimensions("server", "product")
    productFlavors{
        create("free") {
            dimension = "product"
        }
        create("pro") {
            dimension = "product"
        }
        create("serverTest"){
            dimension = "server"
        }
        create("serverRelease") {
            dimension = "server"
        }
    }
```
**product** 选择 `free` **server** 选择 `serverTest` 时 **BuildType** 为 `debug` 时
<br>
> 优先级越靠后优先级越高

会合并 `main` 、`serverTest` 、 `free` 、`serverTestFree`、 `serverTestFreeDebug`文件夹下的同名 json 文件生成对应的 Kotlin/Java 文件

## gradle 中动态配置
除了在 config 文件夹下配置 json 文件外，还可以在 gradle 中动态配置
::: code-group
```kotlin [build.gradle.kts]
configWeaver {
    customObject {
        create("SampleConfig") { //添加一个 SampleConfig 对象
            addProperty("value_string", "hello configWeaver") //添加一个属性
            addProperty("value_int", 1)
            addProperty("value_float", 1.1f)
            addProperty("value_boolean", true)
            addObject("sample_sub_config") { //添加一个子对象
                //给当前子对象添加属性
                addProperty("sub_value_string", "hello sub configWeaver") 
            }
            withFlavor("free") {//在编译 free 变体时执行此闭包
                //替换 value_string 属性的值 这里也可以额外添加属性
                addProperty("value_string", "use product free hello configWeaver")
            }
            addListProperty("testList") {//添加一个基础类型的列表
               //添加元素
                add(1)
                add(2)
            }
            addListObject("testListObject") { //添加一个对象列表
                add {
                    //给当前 item 添加属性
                    addProperty("test_child_1", "hello configWeaver")
                    addProperty("test_child_2", 1)
                }
                add {
                    addProperty("test_child_2", 3)
                }
            }
        }
}
```
```groovy [build.gradle]
configWeaver {
    customObject {
        SampleConfig {
            addProperty "value_string", "hello configWeaver"
            addProperty "value_int", 1
            addProperty "value_float", 1.1f
            addProperty "value_boolean", true
            addObject("sample_sub_config") { config ->
                config.addProperty("sub_value_string", "hello sub configWeaver")
            }
            withFlavor("free"){freeFlavor ->
                freeFlavor.addProperty("value_string", "use product free hello configWeaver")
            }
            addListProperty("testList"){ testList ->
                testList.add(1)
                testList.add(2)
            }
            addListObject("testListObject") { testListObject ->
                testListObject.add { item ->
                    item.addProperty "test_child_1", "hello configWeaver"
                    item.addProperty "test_child_2", 1
                }
                testListObject.add { item ->
                    item.addProperty "test_child_2", 3
                }
            }
        }
    }
}
```
:::
编译时将会生成以下Kotlin 文件
```kotlin
package com.location.configgen

import kotlin.Boolean
import kotlin.Float
import kotlin.Int
import kotlin.String
import kotlin.collections.List


public object SampleConfig {
  /**
   * key:value_string value:use product free hello configWeaver
   */
  public const val valueString: String = "use product free hello configWeaver"

  /**
   * key:value_int value:1
   */
  public const val valueInt: Int = 1

  /**
   * key:value_float value:1.1
   */
  public const val valueFloat: Float = 1.1f

  /**
   * key:value_boolean value:true
   */
  public const val valueBoolean: Boolean = true

  /**
   * key:testList value:[1, 2]
   */
  public val testList: List<Int> by lazy {
    listOf(1, 2)
  }

  /**
   * key:testListObject value:[{test_child_1=hello configWeaver, test_child_2=1}, {test_child_2=3}]
   */
  public val testListObject: List<com.location.configgen.SampleConfig.TestListObject> by lazy {
    listOf(com.location.configgen.SampleConfig.TestListObject(testChild1 = "hello configWeaver",
        testChild2 = 1), com.location.configgen.SampleConfig.TestListObject(testChild1 = null,
        testChild2 = 3))

  }

  /**
   * key:sample_sub_config - value:
   */
  public object SampleSubConfig {
    /**
     * key:sub_value_string value:hello sub configWeaver
     */
    public const val subValueString: String = "hello sub configWeaver"
  }

  /**
   * key:testListObject - value:[{test_child_1=hello configWeaver, test_child_2=1},
   * {test_child_2=3}]
   */
  public final data class TestListObject(
    public val testChild1: String?,
    public val testChild2: Int,
  )
}
```
## 使用 json 生成配置

### 添加配置文件 生成 Kotlin
在 app 目录下创建 config 目录，添加如下配置文件
::: code-group
```json [app/config/main/network_config.json]
{
    "base_url": "https://www.xxx.com",
    "print_log": true,
    "timeout": {
        "read": 1000,
        "write": 1000,
        "connect": 1000
    }
}
```
```json [app/config/main/feature_config.json]
{
 "enable_log": true,
 // 是否显示支付页面
 "show_pay_page": true,
}
```
::: 
在重新 build 后会在 app/build/generated/source/configWeaver 目录下生成如下 Kotlin 文件

::: code-group
```kotlin [NetworkConfig.kt]
public object NetworkConfig {
  /**
   * key:base_url value:https://www.xxx.com
   */
  public const val baseUrl: String = "https://www.xxx.com"

  /**
   * key:print_log value:true
   */
  public const val printLog: Boolean = true

  /**
   * key:timeout - value:{"read":1000,"write":1000,"connect":1000}
   */
  public object Timeout {
    /**
     * key:read value:1000
     */
    public const val read: Int = 1000

    /**
     * key:write value:1000
     */
    public const val write: Int = 1000

    /**
     * key:connect value:1000
     */
    public const val connect: Int = 1000
  }
}
```
```kotlin [FeatureConfig.kt]
public object FeatureConfig {
  /**
   * key:enable_log value:true
   */
  public const val enableLog: Boolean = true

  /**
   * key:show_pay_page value:true
   */
  public const val showPayPage: Boolean = true
}
```
:::

<details>
  <summary>查看代码2</summary>

``` vue
<template>
  <el-button type="primary">主要按钮</el-button>
  <el-button type="success">绿色按钮</el-button>
  <el-button type="info">灰色按钮</el-button>
  <el-button type="warning">黄色按钮</el-button>
  <el-button type="danger">红色按钮</el-button>
</template>
```

</details>

## 4. 多变体 BuildType 支持
在上方 network_config.json 配置网络请求使用的 host
如果我们希望在 debug 包中使用测试环境 

在 app/config/debug/中创建network_config.json 添加如下配置
```json{2}
{
    //覆盖 app/config/main/network_config.json 中的base_url属性
    "base_url": "https://www.xxx-test.com",
}
```
选择编译 debug 包 将会生成如下 NetworkConfig.kt 代码
```kotlin{3,5}
public object NetworkConfig {
  /**
   * key:base_url value:https://www.xxx-test.com
   */
  public const val baseUrl: String = "https://www.xxx-test.com"// [!code focus]

  /**
   * key:print_log value:true
   */
  public const val printLog: Boolean = true

  /**
   * key:timeout - value:{"read":1000,"write":1000,"connect":1000}
   */
  public object Timeout {
    /**
     * key:read value:1000
     */
    public const val read: Int = 1000

    /**
     * key:write value:1000
     */
    public const val write: Int = 1000

    /**
     * key:connect value:1000
     */
    public const val connect: Int = 1000
  }
}
```
可以看到 最终生成的配置会将 **main**、**debug** 两个文件夹内的同名 json文件进行合并






