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

private const val EXEGESIS = "//"
private const val FILE_NAME = "@file:"

fun readJsonFile(path: String): JsonData {
    val file = File(path)
    val sb = StringBuilder()
    val lineList = file.readLines(charset = Charsets.UTF_8)
    var fileName: String? = null
    lineList.forEach {
        if (it.trim().startsWith(EXEGESIS)) {
            return@forEach
        }
        if (it.trim().startsWith(FILE_NAME)) {
            fileName = it.substring(FILE_NAME.length)
            if(fileName.isNullOrBlank()){
                fileName = null
            }
            return@forEach
        }
        sb.append(it)
    }
    return JsonData(
        FileHeader(
            fileName ?: file.nameWithoutExtension.className,
            classNameAutoGenerate = fileName == null
        ), sb.toString()
    )
}

data class JsonData(
    val fileHeader: FileHeader,
    val jsonStr:String
)





