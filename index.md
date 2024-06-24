---
layout: home

hero:
  name: "ConfigWeaver"
  text: "Android Gradle 插件 编译期生成源代码"
  tagline: 生成结构化的配置代码
  actions:
    - theme: brand
      text: 指南
      link: /markdown-examples
    - theme: alt
      text: GitHub
      link: https://github.com/location-txl/ConfigWeaver
    - theme: alt
      text: 更新日志
      link: /api-examples

features:
  - title: 使用 JSON 描述配置
    details: 编译时先将各个同名的配置文件合并，再生成对应的Kotlin Java 类。
  - title: 支持 Gradle 动态创建配置 
    details: 在 build.gradle 中动态创建配置，生成对应的 Kotlin Java 类。
  - title: 多渠道打包支持
    details: 根据当前编译的变体 智能合并配置文件，生成对应的 Kotlin Java 类。

 ### Title <Badge type="tip" text="^1.9.0" />
   
---


<!--@include: ./guide/introd/quick_start.md-->

