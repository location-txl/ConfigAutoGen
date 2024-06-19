package com.location.configgen

import com.location.configgen.core.BaseConfigWeaverPlugin
import com.location.configgen.core.CreateClassGenerateFunc
import com.location.confimerge_java.JavaClassGenerate

class ConfigWeaverJavaPlugin : BaseConfigWeaverPlugin() {
    override val createClassGenerate: CreateClassGenerateFunc
        get() = { rootPackageName, outputDir, rootNode, rootClassName ->
            JavaClassGenerate(rootPackageName, outputDir, rootNode, rootClassName)
        }
}