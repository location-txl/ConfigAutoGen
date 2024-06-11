package com.location.configgen.core

import com.android.build.gradle.AppExtension
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
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
class ConfigGenPlugin : Plugin<Project> {
    companion object {
        private const val DEBUG = true
    }

    override fun apply(project: Project) {
        debug {
            println("ConfigGenPlugin apply")
        }

        val android: BaseExtension = project.extensions.getByType(BaseExtension::class.java)
        forEachVariant(android){
            /**
             * productFlavors 排序越靠前，优先级越高
             */
            val defaultPackage = android.defaultConfig.applicationId ?: "com.location.config"
            if (DEBUG) {
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
            val mergeDirs = mutableListOf<String>().run {
                add("main")
                it.productFlavors.toMutableList().also {productFlavors ->
                    productFlavors.reverse()
                    productFlavors.forEach {productFlavor ->
                        add(productFlavor.name)
                    }
                }
                if(it.flavorName.isNotBlank()){
                    add(it.flavorName)
                }
                add(it.buildType.name)
                add(it.name)
                toList()
            }

            val task = project.tasks.create(
                "generate${
                    it.name.replaceFirstChar {name ->
                        if (name.isLowerCase()) name.uppercase() else name.toString()
                    }
                }Config",
                ConfigGenTask::class.java
            ) { task ->
                task.sourceDirs.set(project.files(mergeDirs.map { mergeName ->
                    project.file("src/config/${mergeName}")
                }))
                task.outputDir.set(File("${project.configMergeJavaSourceDir}${it.name}${File.separator}"))
                task.debug = DEBUG
                task.packageName = defaultPackage
            }

            if (DEBUG) {
                println("mergeDirs = $mergeDirs")
            }
            it.registerJavaGeneratingTask(task, task.outputDir.get().asFile)

        }
    }


    @Suppress("DEPRECATION")
    private fun forEachVariant(
        extension: BaseExtension,
        action: (com.android.build.gradle.api.BaseVariant) -> Unit
    ) {
        when (extension) {
            is AppExtension -> extension.applicationVariants.all(action)
            is LibraryExtension -> {
                extension.libraryVariants.all(action)
            }
            else -> throw GradleException(
                "config gen must use android app or library"
            )
        }
    }


    private inline fun <T> debug(block: () -> T): T? {
        return if (DEBUG) {
            block()
        } else {
            null
        }
    }
}