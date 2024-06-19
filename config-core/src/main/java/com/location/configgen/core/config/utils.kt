package com.location.configgen.core.config

/**
 *
 * @author tianxiaolong
 * time：2024/6/21 16:37
 * description：
 */


private val reg = Regex("^[a-zA-Z_][a-zA-Z0-9_]*$")
fun checkPropertyValid(key: String) {
    if (!reg.matches(key)) {
        error("property '$key' is incorrect naming  you should use [a-zA-Z_][a-zA-Z0-9_]*")
    }
}