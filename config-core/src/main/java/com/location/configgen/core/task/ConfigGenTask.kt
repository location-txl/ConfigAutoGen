package com.location.configgen.core.task

import com.google.gson.Gson
import com.location.configgen.core.CreateClassGenerateFunc
import com.location.configgen.core.codeGen.className
import com.location.configgen.core.config.ConfigHeader
import com.location.configgen.core.config.JsonData
import com.location.configgen.core.config.checkPropertyValid
import com.location.configgen.core.config.defaultParseConfig
import com.location.configgen.core.datanode.toNode
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Console
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import java.io.File

/**
 *
 * @author tianxiaolong
 * time：2024/6/7 10:22
 * description：
 */
//@CacheableTask
abstract class ConfigGenTask : DefaultTask() {

    @get:PathSensitive(PathSensitivity.RELATIVE)
    @get:InputFiles
    val sourceDirs: ListProperty<File> = project.objects.listProperty(File::class.java)


    @OutputDirectory
    val outputDir: DirectoryProperty = project.objects.directoryProperty()

    @Console
    var debug: Boolean = false

    @Input
    var packageName:String = ""

    @get:Internal
    var createClassGenerateFunc:CreateClassGenerateFunc? = null


    @TaskAction
    fun genCode(){
        if(debug){
            println("ConfigGenTask genCode")
        }
        require(createClassGenerateFunc != null)
        if(debug){
            sourceDirs.get().forEach {
                println("sourceDir = ${it.absolutePath}")
            }
            println("outputDir = ${outputDir.get().asFile.absolutePath}")
        }

        val configSourceList = mergeFiles()
        outputDir.get().asFile.deleteRecursively()


        configSourceList.forEach {
            checkPropertyValid(it.configHeader.className, "config json file")
            createClassGenerateFunc?.invoke(
                project,
                packageName,
                outputDir.asFile.get().absolutePath,
                defaultParseConfig.parseConfig(it.json),
                it.configHeader.className
            )?.create()
        }


    }




    private fun mergeFiles(): List<ConfigSource> {
        val fileMaps = mutableMapOf<String, MutableList<String>>()
        sourceDirs.get().filter { it.isDirectory }.forEach { dir ->
            println("dir = ${dir.listFiles()}")
            dir.listFiles { _, fileName ->
                defaultParseConfig.isValidFile(fileName)
            }?.forEach { file ->
                fileMaps.getOrPut(file.nameWithoutExtension) { mutableListOf() }
                    .add(file.absolutePath)
            }
        }
        if(debug){
            fileMaps.forEach { (t, u) ->
                println("key = $t")
                u.forEach {
                    println("value = $it")
                }
            }
        }
        val configSourcesList = mutableListOf<ConfigSource>()
        fileMaps.forEach { (_, pathList) ->
            val config = defaultParseConfig.mergeConfig(pathList)
            println("pathList:$pathList")
            println("config = ${config.attribute}")

            val configHeader = ConfigHeader(
                config.attribute.className ?: File(pathList.first()).nameWithoutExtension.className
            )
            if (debug) {
                println("mergeJson = ${config.content}")
            }
            configSourcesList.add(ConfigSource(config.content, configHeader))
        }
        return configSourcesList.toList()
    }






    data class ConfigSource(
        val json: String, val configHeader: ConfigHeader
    )


}

