package com.location.configgen.core.config

import com.location.configgen.core.codeGen.className
import java.io.File

/**
 *
 * @author tianxiaolong
 * time：2023/7/19 17:43
 * description：
 */
data class FileHeader(
    val className: String,
    val classNameAutoGenerate: Boolean
)


data class JsonData(
    val fileHeader: FileHeader,
    val jsonStr:String
)





