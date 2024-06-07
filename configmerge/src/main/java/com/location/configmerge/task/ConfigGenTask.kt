package com.location.configmerge.task

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

/**
 *
 * @author tianxiaolong
 * time：2024/6/7 10:22
 * description：
 */
abstract class ConfigGenTask:DefaultTask() {



    @InputFiles
    val souceDirs = project.objects.listProperty(File::class.java)


    @OutputDirectory
    val outputDir = project.objects.directoryProperty()




    @TaskAction
    fun genCode(){
        println("ConfigGenTask genCode")
        souceDirs.get().forEach {
            println("sourceDir = ${it.absolutePath}")
        }
        println("outputDir = ${outputDir.get().asFile.absolutePath}")
    }

}