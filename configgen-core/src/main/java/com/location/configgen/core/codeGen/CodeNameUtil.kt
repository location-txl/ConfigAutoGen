package com.location.configgen.core.codeGen


/**
 *
 * @author tianxiaolong
 * time：2024/6/14 17:28
 * description：
 */

val String.methodName
    get() = "get${this.className}"

val String.className
    get() = if (this.contains("_")) {
        this.split("_").joinToString("") {
            it.replaceFirstChar { firstChar ->
                if (firstChar.isLowerCase()) firstChar.uppercase() else firstChar.toString()
            }
        }
    } else {
        this.replaceFirstChar { if (it.isLowerCase()) it.uppercase() else it.toString() }
    }


val String.fieldName
    get() = if (this.contains("_")) {
        this.split("_").mapIndexed { index: Int, str: String ->
            str.replaceFirstChar { char -> if (index == 0) char.lowercase() else char.uppercase() }
        }.joinToString("")
    } else {
        this.replaceFirstChar {
            if (it.isUpperCase()) {
                it.lowercase()
            } else {
                it.toString()
            }
        }
    }

