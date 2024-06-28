package com.location.configgen.core.task

import com.location.configgen.core.CreateClassGenerateFunc
import com.location.configgen.core.codeGen.ClassGenerate
import com.location.configgen.core.config.ConfigHeader
import com.location.configgen.core.config.JsonData
import com.location.configgen.core.config.checkPropertyValid
import com.location.configgen.core.config.readJsonFile
import com.location.configgen.core.datanode.Node
import com.location.configgen.core.datanode.toNode
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Console
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
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
abstract class ConfigGenTask : DefaultTask() {

    @InputFiles
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
        val jsonParser = JSONParser()

        configSourceList.forEach {
            checkPropertyValid(it.configHeader.className)
            createClassGenerateFunc?.invoke(
                packageName,
                outputDir.asFile.get().absolutePath,
                (jsonParser.parse(it.json) as? JSONObject)?.toNode()
                    ?: error("json config only support first element is JsObj"),
                it.configHeader.className
            )?.create()
        }


    }




    private fun mergeFiles(): List<ConfigSource> {
        val fileMaps = mutableMapOf<String, MutableList<String>>()
        sourceDirs.get().filter { it.isDirectory }.forEach { dir ->
            println("dir = ${dir.listFiles()}")
            dir.listFiles { _, s -> s.endsWith(".json") }?.forEach { file ->
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
        fileMaps.forEach { (t, u) ->
            val mergeJson = mergeJson(u)
            if (mergeJson != null) {
                val configHeader = ConfigHeader(
                    mergeJson.fileHeader.className,
                    mergeJson.fileHeader.classNameAutoGenerate
                )
                if(debug){
                    println("mergeJson = ${mergeJson.jsonStr}")
                }
                configSourcesList.add(ConfigSource(mergeJson.jsonStr, configHeader))
            }else{
                throw IllegalArgumentException("merge json fail")
            }
        }
        return configSourcesList.toList()
    }


    private fun mergeJson(listFiles: List<String>): JsonData? {
        val jsonParser = JSONParser()
        return listFiles.fold(null) { oldData: JsonData?, file: String ->
            val newData = readJsonFile(file)
            if (oldData == null) {
                newData
            } else {
                val oldObj = jsonParser.parse(oldData.jsonStr)
                val newObj = jsonParser.parse(newData.jsonStr)
                if (oldObj.javaClass.simpleName != newObj.javaClass.simpleName) {
                    throw IllegalArgumentException("json type not match s1 path:${file} type:${oldObj.javaClass.simpleName} s2 path:${oldData.jsonStr} type:${newObj.javaClass.simpleName}")
                }
                if (oldObj is JSONObject) {
                    mergeJsObj(oldObj, newObj as JSONObject)
                } else if (oldObj is JSONArray) {
                    mergeJsArray(oldObj, newObj as JSONArray)
                }
                oldData.copy(
                    fileHeader = if (newData.fileHeader.classNameAutoGenerate.not()) {
                        oldData.fileHeader
                    } else {
                        newData.fileHeader
                    }, jsonStr = oldObj.toString()
                )
            }
        }
    }

    private fun mergeJsObj(source:JSONObject, merge:JSONObject){
        source.forEach{k,v ->
            if(v is JSONObject){
                val mV = merge.remove(k) ?: return@forEach
                if((mV is JSONObject).not()){
                    throw IllegalArgumentException("merge json fail $k type not match mergeType is ${mV.javaClass.simpleName}")
                }
                mergeJsObj(v, mV as JSONObject)
            }else if(v is JSONArray){
                val mV = merge.remove(k) ?: return@forEach
                if((mV is JSONArray).not()){
                    throw IllegalArgumentException("merge json fail $k type not match mergeType is ${mV.javaClass.simpleName}")
                }
                mergeJsArray(v, mV as JSONArray)
            }
        }
        source.putAll(merge)
    }

    private fun mergeJsArray(source: JSONArray, mergeValue: JSONArray) {
        source.clear()
        source.addAll(mergeValue)
    }


    data class ConfigSource(
        val json: String, val configHeader: ConfigHeader
    )


}