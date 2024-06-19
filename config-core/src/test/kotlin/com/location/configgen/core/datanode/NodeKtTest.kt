package com.location.configgen.core.datanode

import com.google.common.truth.Truth.assertThat
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import org.junit.Test

/**
 *
 * @author tianxiaolong
 * time：2024/6/19 19:22
 * description：
 */
class NodeKtTest {


    @Test
    fun testJsonToNode() {
        val json = """
       {
            "name": "tom",
            "age": 1,
            "test_null": null,
            "obj": {
                "name": "1",
                "test_null": null,
                "sub_obj": {
                    "sub_id": 1,
                    "ext": {
                        "id": 1
                    }
                },
                "ids": [
                    1,
                    2,
                    3,
                    4,
                    5,
                    6
                ],
                "objInfo": [
                    {
                        "id": 1,
                        "a": "2"
                    },
                    {
                        "id": 5,
                        "a": "6"
                    }
                ]
            },
            "u_ids": [
                "u_1",
                "u_2"
            ],
            "tags": [
                {
                    "id": 1,
                    "name": "1234",
                    "child_null": null,
                    "tagObj": {
                        "obj1": 1,
                        "obj2": 2.1
                    },
                    "childs": [
                        {
                            "id": 1,
                            "name": "child1"
                        },
                        {
                            "id": 1,
                            "name": "child2"
                        }
                    ]
                }
            ]
        }
         """.trimIndent()
        val jsonParser = JSONParser()
        val obj = jsonParser.parse(json) as JSONObject
        val node = obj.toNode()
        val expectedNode = generateExpectedNode()
        val property = node.property
        equalsNode(node, expectedNode)


    }


    private fun equalsNode(n1: Node.ObjectNode, n2: Node.ObjectNode) {
        val p1 = n1.property.toMutableMap()
        val p2 = n2.property.toMutableMap()
        p1.forEach { (k, v) ->
            when (v) {
                is Node.ValueNode -> {
                    if (p2.remove(k) != v) {
                        error("test error")
                    }
                }

                is Node.ListNode -> {
                    val remove = p2.remove(k)
                    if (remove == null || remove !is Node.ListNode) {
                        error("test error")
                    }
                    if (v.list.size != remove.list.size) {
                        error("testerror")
                    }
                }

                is Node.ObjectNode -> {
                    val remove = p2.remove(k)
                    if (remove == null || remove !is Node.ObjectNode) {
                        error("test error")
                    }
                    equalsNode(v, remove)
                }

                null -> {
                    val remove = p2.remove(k)
                    if (remove != null) {
                        error("test error")
                    }
                }
            }

        }
        if (p2.isNotEmpty()) {
            error("2")
        }
    }

    private val Any.valueNode: Node.ValueNode
        get() = Node.ValueNode(this)

    private fun generateExpectedNode(): Node.ObjectNode {
        val nodeMaps = mutableMapOf<String, Node?>()
        nodeMaps["name"] = Node.ValueNode("tom")
        nodeMaps["age"] = Node.ValueNode(1L)
        nodeMaps["test_null"] = null
        nodeMaps["obj"] = Node.ObjectNode(mutableMapOf<String, Node?>().also { objMap ->
            objMap["name"] = Node.ValueNode("1")
            objMap["test_null"] = null
            objMap["sub_obj"] = Node.ObjectNode(mutableMapOf<String, Node?>().also { subMap ->
                subMap["sub_id"] = Node.ValueNode(1L)
                subMap["ext"] = Node.ObjectNode(mutableMapOf<String, Node?>().also { extMap ->
                    extMap["id"] = Node.ValueNode(1L)
                })
            })
            objMap["ids"] = Node.ListNode(
                listOf(
                    Node.ValueNode(1L),
                    Node.ValueNode(2L),
                    Node.ValueNode(3L),
                    Node.ValueNode(4L),
                    Node.ValueNode(5L),
                    Node.ValueNode(6L),
                )
            )
            objMap["objInfo"] = Node.ListNode(
                listOf(
                    Node.ObjectNode(
                        mutableMapOf<String, Node?>(
                            "id" to Node.ValueNode(1L),
                            "a" to Node.ValueNode("2"),
                        )
                    ),
                    Node.ObjectNode(
                        mutableMapOf<String, Node?>(
                            "id" to Node.ValueNode(5L),
                            "a" to Node.ValueNode("6"),
                        )
                    ),
                )
            )
        })

        nodeMaps["u_ids"] = Node.ListNode(
            listOf(
                Node.ValueNode("u_1"),
                Node.ValueNode("u_2"),
            )
        )

        nodeMaps["tags"] = Node.ListNode(
            listOf(
                Node.ObjectNode(
                    mutableMapOf(
                        "id" to Node.ValueNode(1L),
                        "name" to Node.ValueNode(1234L),
                        "child_null" to null,
                        "tagObj" to Node.ObjectNode(
                            mutableMapOf(
                                "obj1" to Node.ValueNode(1L),
                                "obj2" to Node.ValueNode(2.1),
                            )
                        ),
                        "childs" to Node.ListNode(
                            listOf(
                                Node.ObjectNode(
                                    mutableMapOf(
                                        "id" to Node.ValueNode(1L),
                                        "name" to Node.ValueNode("child1"),
                                    )
                                ),
                                Node.ObjectNode(
                                    mutableMapOf(
                                        "id" to Node.ValueNode(1L),
                                        "name" to Node.ValueNode("child2"),
                                    )
                                ),

                                )
                        )
                    )
                )

            )
        )
        return Node.ObjectNode(nodeMaps)

    }
}