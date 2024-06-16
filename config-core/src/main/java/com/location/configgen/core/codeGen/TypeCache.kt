package com.location.configgen.core.codeGen

import com.location.configgen.core.datanode.ValueType

/**
 *
 * @author tianxiaolong
 * time：2024/6/11 17:48
 * description：
 */


data class JsArrayType(val type: Any, val canNull: Boolean, val isList: Boolean = false)







sealed class DataType(val rawKey: String, val canNull: Boolean, val isList: Boolean) {
    class BasisType(val type: ValueType, rawKey: String, canNull: Boolean, isList: Boolean) :
        DataType(rawKey, canNull, isList)

    class ObjectType(
        val pkgName: String, //包名
        val className: String, //类名
        val dataTypeMap: Map<String, DataType>, //字段名和类型的映射
        rawKey: String, canNull: Boolean, isList: Boolean
    ) : DataType(rawKey, canNull, isList)

    class UnknownType(rawKey: String, canNull: Boolean, isList: Boolean) :
        DataType(rawKey, canNull, isList)
}

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