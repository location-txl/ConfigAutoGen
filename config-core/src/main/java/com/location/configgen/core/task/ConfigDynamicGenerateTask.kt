package com.location.configgen.core.task

import com.location.configgen.core.CreateClassGenerateFunc
import com.location.configgen.core.codeGen.ClassGenerate
import com.location.configgen.core.codeGen.className
import com.location.configgen.core.datanode.Node
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

open class ConfigDynamicGenerateTask: DefaultTask() {


    @Input
    var nodeList:Map<String, Node.ObjectNode> = mapOf()

    @OutputDirectory
    val outputDir: DirectoryProperty = project.objects.directoryProperty()

    @Input
    var packageName:String = ""


    @get:Internal
    var createClassGenerateFunc:CreateClassGenerateFunc? = null


    @TaskAction
    fun genCode(){
        require(createClassGenerateFunc != null)
        outputDir.get().asFile.deleteRecursively()
        nodeList.forEach { (k,v) ->
            createClassGenerateFunc?.invoke(
                packageName,
                outputDir.asFile.get().absolutePath,
                v,
                k.className
            )?.create()
        }
    }
}


