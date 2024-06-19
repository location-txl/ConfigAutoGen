package com.location.configgen.core

/**
 *
 * @author tianxiaolong
 * time：2024/6/19 15:30
 * description：
 */
open class KotlinExtension : BaseConfigWeaverExtension() {
    val kotlin: KotlinConfig = KotlinConfig()

    fun kotlin(action: KotlinConfig.() -> Unit) {
        kotlin.action()
    }
}

class KotlinConfig {
    var jvmTT: String? = null
}