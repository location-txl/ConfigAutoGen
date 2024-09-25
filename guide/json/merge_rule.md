# 合并规则

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
## 不同编译配置支持的编译文件夹
支持的文件夹名字
1. `main`文件夹是主文件夹 优先级最低
2. 自定义的`productFlavor`文件夹
3. `BuildType`文件夹
4. `productFlavor`组合后的文件夹
5. `productFlavor`和`BuildType`组合后的文件夹

举例
```kotlin
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
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
```
当前我准备以**free + serverTest + release**构建出一个apk时，合并规则如下
在config文件夹中支持编译的文件夹名字如下(**越靠后优先级越高**)
- main
- free
- serverTest
- serverTestFree
- release
- serverTestFreeRelease
## 文件合并规则
1. 相同路径下同名`json`文件会被合并
2. object结构体里面的属性会根据优先级进行属性替换
3. 数组结构体会进行替换

举例：在main文件夹下面有一个`net_config.json`文件
```json
{
  "baseUrl": "https://main-test.com",
  "timeout": {
    "connect": 10000,
    "read": 10000,
    "write": 10000
    }
}
```
在release 文件夹下面有一个同名`net_config.json`文件
```json
{
  "baseUrl": "https://release-pro.com",
  "cache": true,
  "timeout": {
    "write": 20000
    }
}
```
在编译的BuildType为`release`时，合并后的json如下
```json{2,3,7}
{
  "baseUrl": "https://release-pro.com",
  "cache": true,
  "timeout": {
    "connect": 10000,
    "read": 10000,
    "write": 20000
    }
}
```
可以看到
- `baseUrl`属性被替换
- `cache`属性被添加
- `timeout`属性中`write`被替换

> [!note]
> 需要注意的时 合并json文件时无法删除属性，只能替换或者添加属性



