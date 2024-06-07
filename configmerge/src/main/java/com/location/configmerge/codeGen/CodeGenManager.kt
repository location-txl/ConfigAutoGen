package com.location.configmerge.codeGen

import com.location.configmerge.ConfigGenTask

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

