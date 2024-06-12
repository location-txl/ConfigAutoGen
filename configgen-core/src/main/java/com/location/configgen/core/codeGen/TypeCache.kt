package com.location.configgen.core.codeGen

import org.json.simple.JSONArray

/**
 *
 * @author tianxiaolong
 * time：2024/6/11 17:48
 * description：
 */


data class JsArrayType(val type: Any, val isNull: Boolean, val isList:Boolean = false)






data class AA(
    val bools: List<Boolean>,
    val config: Config,
    val floatIds: List<Double>,
    val ids: List<String>,
    val intIds: List<Int>,
    val name: String,
    val users: List<User>
)

data class Config(
    val develop: Boolean,
    val release_url: String
)

data class User(
    val age: Int,
    val config: List<String>?,
    val email: String?,
    val like: Int?,
    val name: String?
)