package com.location.configgen.core

import com.location.configgen.core.codeGen.ClassGenerate
import com.location.configgen.core.datanode.Node
import org.gradle.api.Project
import java.io.File

/**
 *
 * @author tianxiaolong
 * time：2024/6/6 21:06
 * description：
 */



fun Project.getConfigWeaverSourceDir(key: String):String =
    "${layout.buildDirectory.asFile.get().absolutePath}${File.separator}generated${File.separator}source${File.separator}configWeaver${File.separator}$key${File.separator}"


typealias CreateClassGenerateFunc = (
    rootPackageName: String,
    outputDir: String,
    rootNode: Node.ObjectNode,
    rootClassName: String,
) -> ClassGenerate<*>