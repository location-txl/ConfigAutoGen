package com.location.configgen.core.datanode

import com.location.configgen.core.codeGen.className
import java.lang.reflect.Type


/**
 *
 * @author tianxiaolong
 * time：2024/6/7 11:05
 * description：
 */
sealed interface Node{
    data class ObjectNode(
        val name: String,
        val property: MutableMap<String, Node>,
    ) : Node
    data class ValueNode(
        val value: Any,
        val type: Type,
    ) : Node {
        enum class Type(val typeName: String) {
            STRING("String"),
            INT("Int"),
            BOOLEAN("Boolean"),
            DOUBLE("Double"),
            LONG("Long"),
            FLOAT("Float"),
        }
    }
}

val Any.nodeType: Node.ValueNode.Type
    get() = when (this) {
        is String -> Node.ValueNode.Type.STRING
        is Int -> Node.ValueNode.Type.INT
        is Boolean -> Node.ValueNode.Type.BOOLEAN
        is Double -> if (this > Float.MAX_VALUE) Node.ValueNode.Type.DOUBLE else Node.ValueNode.Type.FLOAT
        is Long -> if (this > Int.MAX_VALUE) Node.ValueNode.Type.LONG else Node.ValueNode.Type.INT
        is Float -> Node.ValueNode.Type.FLOAT
        else -> throw IllegalArgumentException("Unsupported type: ${this::class.java}")
    }

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


val String.fieldName
    get() = if (this.contains("_")) {
        this.split("_").joinToString("") {
            it.replaceFirstChar { firstChar ->
                if (firstChar.isUpperCase()) firstChar.lowercase() else firstChar.toString()
            }
        }

    } else {
        this.replaceFirstChar {
            if (it.isUpperCase()) {
                it.lowercase()
            } else {
                it.toString()
            }
        }
    }


val String.methodName
    get() = "get${this.className}"