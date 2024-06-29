package com.location.configgen.core.config

/**
 *
 * @author tianxiaolong
 * time：2024/6/21 16:37
 * description：
 */


private val reg = Regex("^[a-zA-Z_][a-zA-Z0-9_]*$")
fun checkPropertyValid(key: String, prefix:String = "property"): String {
    if (!reg.matches(key)) {
        error("$prefix '$key' is incorrect naming  check reg:$reg")
    }
    return key
}