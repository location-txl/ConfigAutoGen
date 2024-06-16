package com.location.configgen.core.datanode

import java.lang.reflect.Type


/**
 *
 * @author tianxiaolong
 * time：2024/6/7 11:05
 * description：
 */




val Any.valueType: ValueType
    get() = when (this) {
        is String -> ValueType.STRING
        is Int -> ValueType.INT
        is Boolean -> ValueType.BOOLEAN
        is Double -> if (this > Float.MAX_VALUE) ValueType.DOUBLE else ValueType.FLOAT
        is Long -> if (this > Int.MAX_VALUE) ValueType.LONG else ValueType.INT
        is Float -> ValueType.FLOAT
        else -> throw IllegalArgumentException("Unsupported type: ${this::class.java}")
    }


/**
 *
 * @param typeName 类型名称
 * @param type 类型
 * @param groupId 类型分组 相同分组的类型可以向上提升
 */
enum class ValueType(val typeName: String, val type:Class<*>, val groupId:Int) {
    STRING("String", String::class.java, 0),
    INT("Int", Int::class.java, 1),
    LONG("Long", Long::class.java, 1),
    BOOLEAN("Boolean", Boolean::class.java, 2),
    FLOAT("Float", Float::class.java, 3),
    DOUBLE("Double", Double::class.java, 3),
}





