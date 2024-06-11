package com.location.configgen.core.datanode


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







