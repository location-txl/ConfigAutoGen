package com.location.configgen.core

import com.android.build.gradle.api.BaseVariant
import com.location.configgen.core.codeGen.ClassGenerate
import com.location.configgen.core.datanode.Node
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider
import java.io.File

/**
 *
 * @author tianxiaolong
 * time：2024/6/6 21:06
 * description：
 */


fun Project.getConfigWeaverSourceDir(variant: BaseVariant, key: String): Provider<Directory> =
    layout.buildDirectory.dir("generated/source/configWeaver/${variant.dirName}/$key")



typealias CreateClassGenerateFunc = (
    rootPackageName: String,
    outputDir: String,
    rootNode: Node.ObjectNode,
    rootClassName: String,
) -> ClassGenerate<*>