package com.location.configgen.core

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.tasks.factory.TaskConfigAction
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
const val MIN_AGP_VERSION = "4.2.2"
const val MIN_KOTLIN_VERSION = "1.8.0"
const val MIN_GRADLE_VERSION = "6.7.1"

abstract class BaseConfigWeaverPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val ext = project.extensions.create(
            "configWeaver", extensionClass, project
        ) as BaseConfigWeaverExtension

        applyExtension(ext)
        val android: BaseExtension = project.extensions.getByType(BaseExtension::class.java)
        val agpVersion = project.rootProject.buildscript.configurations.getByName("classpath")
            .dependencies.all {
                println("classpath = $it")
            }

        val gradleVersion = project.gradle.gradleVersion
        if (gradleVersion < MIN_GRADLE_VERSION) {
            throw GradleException("gradle version must be $MIN_GRADLE_VERSION or higher")
        }
        forEachVariant(android) { variant ->

            val defaultPackage = debugFlavor(android, ext, variant)

            /**
             * 排序越靠后，优先级越高
             */
            val flavors = mutableListOf<String>().run {
                variant.productFlavors.toMutableList().also { productFlavors ->
                    productFlavors.reverse()
                    productFlavors.forEach { productFlavor ->
                        add(productFlavor.name)
                    }
                }
                if (variant.flavorName.isNotBlank()) {
                    add(variant.flavorName)
                }
                add(variant.buildType.name)
                add(variant.name)
                toList()
            }
            eachVariant(flavors, variant)
            val suffix = variant.name.replaceFirstChar { name ->
                if (name.isLowerCase()) name.uppercase() else name.toString()
            }
            val parseJsonTask = project.tasks.registerTask("configWeaverGenerate${
                suffix
            }Config",
                ConfigGenTask::class.java,
                action = object : TaskConfigAction<ConfigGenTask> {
                    override fun configure(task: ConfigGenTask) {
                        with(task) {
                            sourceDirs.add(project.file("config/main"))
                            sourceDirs.addAll(project.files(flavors.map { mergeName ->
                                project.file("config/${mergeName}")
                            }))
                            outputDir.set(project.getConfigWeaverSourceDir(variant, "json"))
                            debug = ext.debugLog
                            packageName = defaultPackage
                            createClassGenerateFunc = createClassGenerate
                        }
                    }
                })


            if (ext.debugLog) {
                println("mergeDirs = $flavors")
            }

            val generateDynamicTask =
                project.tasks.registerTask("configWeaverGenerate${suffix}CustomConfig",
                    ConfigDynamicGenerateTask::class.java,
                    action = object : TaskConfigAction<ConfigDynamicGenerateTask> {
                        override fun configure(task: ConfigDynamicGenerateTask) {
                            with(task) {
                                nodeList =
                                    ext.customObject.fold(mutableMapOf()) { acc, dynamicObject ->
                                        val node = dynamicObject.getObjectNode(flavors)
                                        if (node != null) {
                                            acc[dynamicObject.name] = node
                                        }
                                        acc
                                    }
                                packageName = defaultPackage
                                outputDir.set(project.getConfigWeaverSourceDir(variant, "groovy"))
                                createClassGenerateFunc = createClassGenerate
                            }
                        }

                    })
            variant.registerJavaGeneratingTask(
                generateDynamicTask.get(), generateDynamicTask.get().outputDir.get().asFile
            )
            variant.registerJavaGeneratingTask(
                parseJsonTask.get(), parseJsonTask.get().outputDir.get().asFile
            )
        }

    }

    private fun debugFlavor(
        android: BaseExtension, ext: BaseConfigWeaverExtension, it: BaseVariant
    ): String {
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
        return defaultPackage
    }


    @Suppress("DEPRECATION")
    private fun forEachVariant(
        extension: BaseExtension, action: (BaseVariant) -> Unit
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

    /**
     * 遍历每个变体
     * @param flavor List<String> 当前变体的flavor buildType 组合 越靠后优先级越高
     * @param variant BaseVariant
     */
    protected open fun eachVariant(flavor: List<String>, variant: BaseVariant) {}

    /**
     * 应用扩展属性 子类可强转为自己的扩展类
     * @param extension BaseConfigWeaverExtension
     */
    protected open fun applyExtension(extension: BaseConfigWeaverExtension) {}

    /**
     * 子类重新该方法，返回自己的生成实现类
     * @see ClassGenerate
     */
    abstract val createClassGenerate: CreateClassGenerateFunc


    /**
     * 子类重新该方法，返回自己的扩展类
     */
    open val extensionClass: Class<*>
        get() = BaseConfigWeaverExtension::class.java


}