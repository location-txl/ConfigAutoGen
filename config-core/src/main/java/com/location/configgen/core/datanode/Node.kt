package com.location.configgen.core.datanode

import org.json.simple.JSONArray
import org.json.simple.JSONObject


/**
 *
 * @author tianxiaolong
 * time：2024/6/7 11:05
 * description：
 */


sealed class Node(open val docs: String) {
    data class ObjectNode(
        val property: Map<String, Node?>, override val docs: String,
    ) : Node(docs), Map<String, Node?> by property {
        override fun toString(): String {
            return property.toString()
        }
    }

    data class ListNode(val list: List<Node?>, override val docs: String) : Node(docs),
        List<Node?> by list {
        override fun toString(): String {
            return list.toString()
        }
    }
    data class ValueNode(
        val value: Any,
        override val docs: String,
        val type: ValueType? = null,
    ) : Node(docs) {

        val valueType: ValueType
            get() = when (this.value) {
                is String -> ValueType.STRING
                is Int -> ValueType.INT
                is Boolean -> ValueType.BOOLEAN
                is Double -> if (this.value > Float.MAX_VALUE) ValueType.DOUBLE else ValueType.FLOAT
                is Long -> if (this.value > Int.MAX_VALUE) ValueType.LONG else ValueType.INT
                is Float -> ValueType.FLOAT
                else -> throw IllegalArgumentException("Unsupported type: ${this::class.java}")
            }

        override fun toString(): String {
            return value.toString()
        }


    }
}


fun JSONObject.toNode(): Node.ObjectNode {
    val propertyMap = LinkedHashMap<String, Node?>()

    forEach { k, v: Any? ->
        propertyMap[k.toString()] = parseJsValue(v)
    }
    return Node.ObjectNode(propertyMap.toMap(), this.toString())
}

private fun parseJsValue(
    v: Any?,
): Node? {
    return when (v) {
        is JSONObject -> {
            v.toNode()
        }

        is JSONArray -> {
            val nodeList = mutableListOf<Node?>()
            v.forEach { jsItem ->
                nodeList.add(parseJsValue(jsItem).also { subNode ->
                    if (subNode is Node.ListNode) {
                        error("Not supporting multi-layer nested arrays value:$v")
                    }
                })
            }
            //TODO 检测里面的 item 都是一个类型 否则崩溃
//            nodeList.filterNotNull()
            Node.ListNode(nodeList.toList(), v.toString())
        }

        else -> {
            v?.let { Node.ValueNode(it, docs = "$v") }
        }
    }
}




/**
 *
 * @param typeName 类型名称
 * @param type 类型
 * @param groupId 类型分组 相同分组的类型可以向上提升
 */
enum class ValueType(private val typeName: String, val type: Class<*>, val groupId: Int) {
    STRING("String", String::class.java, 0),
    INT("Int", Int::class.java, 1),
    LONG("Long", Long::class.java, 1),
    BOOLEAN("Boolean", Boolean::class.java, 2),
    FLOAT("Float", Float::class.java, 3),
    DOUBLE("Double", Double::class.java, 3),
}





