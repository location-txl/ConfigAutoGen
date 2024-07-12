package com.location.configgen

import com.location.configgen.core.BaseConfigWeaverPlugin
import com.location.configgen.core.CreateClassGenerateFunc
import com.location.confimerge_kotlin.KotlinClassGenerate

class ConfigWeaverKotlinPlugin : BaseConfigWeaverPlugin() {
    override val createClassGenerate: CreateClassGenerateFunc
        get() = { project, rootPackageName, outputDir, rootNode, rootClassName ->
            KotlinClassGenerate(project, rootPackageName, outputDir, rootNode, rootClassName)
        }


    fun a() {
    }
}