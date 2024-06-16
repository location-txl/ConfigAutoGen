package com.location.configgen

import com.location.confimerge_java.JavaClassGenerate

/**
 *
 * @author tianxiaolong
 * time：2024/6/17 14:58
 * description：
 */
object ClassGenerateProvider {

    @JvmStatic
    fun provider() = JavaClassGenerate::class.java
}