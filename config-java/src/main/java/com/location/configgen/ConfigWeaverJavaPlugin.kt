package com.location.configgen

import com.location.configgen.core.BaseConfigWeaverExtension
import com.location.configgen.core.BaseConfigWeaverPlugin
import com.location.configgen.core.CreateClassGenerateFunc
import com.location.confimerge_java.JavaClassGenerate

class ConfigWeaverJavaPlugin : BaseConfigWeaverPlugin() {
    override val createClassGenerate: CreateClassGenerateFunc
        get() = { rootPackageName, outputDir, rootNode, rootClassName ->
            JavaClassGenerate(rootPackageName, outputDir, rootNode, rootClassName)
        }


    override val extensionClass: Class<*>
        get() = JavaConfigWeaverExtension::class.java


    override fun applyExtension(extension: BaseConfigWeaverExtension) {
        extension as JavaConfigWeaverExtension
        defJavaOptions = extension.javaOptions
    }
}


