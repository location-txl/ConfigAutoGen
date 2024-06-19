package com.location.configgen.core

import com.android.build.gradle.AppExtension
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.tasks.factory.registerTask
import com.location.configgen.core.codeGen.ClassGenerate
import com.location.configgen.core.datanode.Node
import com.location.configgen.core.task.ConfigDynamicGenerateTask
import com.location.configgen.core.task.ConfigGenTask
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File

/**
 *
 * @author tianxiaolong
 * time：2024/6/6 17:25
 * description：
 */
abstract class BaseConfigWeaverPlugin : Plugin<Project> {
    override fun apply(project: Project) {


        val ext = project.extensions.create("configWeaver", extensionClass) as BaseConfigWeaverExtension

        project.afterEvaluate {
            applyExtension(ext)
            val android: BaseExtension = project.extensions.getByType(BaseExtension::class.java)
            forEachVariant(android) {
                /**
                 * productFlavors 排序越靠前，优先级越高
                 */
                val defaultPackage = android.defaultConfig.applicationId ?: "com.location.config"
                if (ext.debugLog) {
                    println("packageName = $defaultPackage")
                    println("variant.name = ${it.name}")
                    println("flavorName isNull:${it.flavorName.isBlank()}")
                    println("flavorName ${it.flavorName}")
                    it.productFlavors.forEach {
                        println("productFlavor name = ${it.name}")
                    }
                }

                /**
                 * 排序越靠后，优先级越高
                 */
                val flavors = mutableListOf<String>().run {
                    it.productFlavors.toMutableList().also { productFlavors ->
                        productFlavors.reverse()
                        productFlavors.forEach { productFlavor ->
                            add(productFlavor.name)
                        }
                    }
                    if (it.flavorName.isNotBlank()) {
                        add(it.flavorName)
                    }
                    add(it.buildType.name)
                    add(it.name)
                    toList()
                }
                eachVariant(flavors, it)
                val suffix = it.name.replaceFirstChar { name ->
                    if (name.isLowerCase()) name.uppercase() else name.toString()
                }
                val task = project.tasks.create(
                    "configWeaverGenerate${
                        suffix
                    }Config",
                    ConfigGenTask::class.java
                ) { task ->
                    task.sourceDirs.add(project.file("src/config/main"))
                    task.sourceDirs.addAll(project.files(flavors.map { mergeName ->
                        project.file("src/config/${mergeName}")
                    }))
                    task.outputDir.set(File("${project.getConfigWeaverSourceDir("json")}${it.name}${File.separator}"))
                    task.debug = ext.debugLog
                    task.packageName = defaultPackage
                    task.createClassGenerateFunc = createClassGenerate

                }

                if (ext.debugLog) {
                    println("mergeDirs = $flavors")
                }

                val generateDynamicTask = project.tasks.registerTask(
                    "configWeaverGenerate${suffix}CustomConfig",
                    ConfigDynamicGenerateTask::class.java
                )
                generateDynamicTask.get().apply {
                    nodeList =
                        ext.customObject.fold(mutableMapOf()) { acc, dynamicObject ->
                            val node = dynamicObject.getObjectNode(flavors)
                            if (node != null) {
                                acc[dynamicObject.name] = node
                            }
                            acc
                        }

                    packageName = defaultPackage
                    outputDir.set(File("${project.getConfigWeaverSourceDir("groovy")}${it.name}${File.separator}"))
                    createClassGenerateFunc = createClassGenerate
                }
                it.registerJavaGeneratingTask(
                    generateDynamicTask,
                    generateDynamicTask.get().outputDir.get().asFile
                )
                it.registerJavaGeneratingTask(task, task.outputDir.get().asFile)
            }
        }
    }



    @Suppress("DEPRECATION")
    private fun forEachVariant(
        extension: BaseExtension,
        action: (BaseVariant) -> Unit
    ) {
        when (extension) {
            is AppExtension -> extension.applicationVariants.all(action)
            is LibraryExtension -> {
                extension.libraryVariants.all(action)
            }

            else -> throw GradleException(
                "ConfigWeaver must use android app or library"
            )
        }
    }

    protected open fun eachVariant(flavor:List<String>, variant: BaseVariant){}

    protected open fun applyExtension(extension: BaseConfigWeaverExtension){}

    abstract val createClassGenerate: CreateClassGenerateFunc





    open val extensionClass:Class<*>
        get() = BaseConfigWeaverExtension::class.java






}