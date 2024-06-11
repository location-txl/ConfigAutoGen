package com.location.configgen.core.codeGen

import com.location.configgen.core.ConfigGenTask

/**
 *
 * @author tianxiaolong
 * time：2024/6/7 17:07
 * description：
 */
class CodeGenManager(
    private val sourceList:List<ConfigGenTask.ConfigSource>,
    private val outputPath:String,
    private val packageName:String

    ) {
    fun genCode(){
        sourceList.forEach {

        }
    }
}

