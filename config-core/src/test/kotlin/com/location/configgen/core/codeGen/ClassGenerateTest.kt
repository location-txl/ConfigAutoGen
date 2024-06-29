package com.location.configgen.core.codeGen

import com.google.common.truth.Truth.assertThat
import com.location.configgen.core.datanode.Node
import com.location.configgen.core.datanode.ValueType
import com.location.configgen.core.helper.TestClassGenerateImpl
import org.junit.Before

import org.junit.Test

/**
 *
 * @author tianxiaolong
 * time：2024/6/28 14:51
 * description：
 */
class ClassGenerateTest {


    @Test
    fun testParseListNodeType() {
        val listNode = Node.ListNode(
            listOf(
                Node.ObjectNode(
                    mapOf(
                        "name" to Node.ValueNode("kotlin", ""),
                        "basicList" to Node.ListNode(
                            listOf(
                                Node.ValueNode(1, ""),
                                Node.ValueNode(2, ""),
                                Node.ValueNode(3, ""),
                            ), ""
                        ),
                        "emptyList" to Node.ListNode(listOf(), ""),
                        "childList" to Node.ListNode(
                            listOf(
                                Node.ObjectNode(mapOf("id" to Node.ValueNode(1, "")), ""),
                                Node.ObjectNode(
                                    mapOf(
                                        "name" to Node.ValueNode("java", ""),
                                        "id" to Node.ValueNode(2, ""),
                                    ), ""
                                ),
                            ), ""
                        ),
                        "subObject" to Node.ObjectNode(
                            mapOf(
                                "id" to Node.ValueNode(1, ""),
                                "name" to Node.ValueNode("subObject", ""),
                                "desc" to null
                            ), ""
                        ),
                    ), ""
                ),
                Node.ObjectNode(
                    mapOf(
                        "id" to Node.ValueNode(1, ""),
                        "name" to null,
                        "basicList" to Node.ListNode(
                            listOf(
                                Node.ValueNode(System.currentTimeMillis(), ""),
                                Node.ValueNode(System.currentTimeMillis(), ""),
                            ), ""
                        ),
                        "childList" to Node.ListNode(
                            listOf(
                                Node.ObjectNode(
                                    mapOf(
                                        "desc" to Node.ValueNode("child desc", ""),
                                        "id" to Node.ValueNode(3, ""),
                                    ), ""
                                ),
                            ), ""
                        ),
                        "subObject" to Node.ObjectNode(
                            mapOf(
                                "desc" to Node.ValueNode("subObject desc", ""),
                            ), ""
                        ),
                    ), ""
                ),
                Node.ObjectNode(
                    mapOf("id" to Node.ValueNode(System.currentTimeMillis(), "")),
                    ""
                ),//判断类型提升
            ), ""
        )
        val objNode = Node.ObjectNode(mapOf("list" to listNode), "")


        var classGenerate =
            createClassGenerate(objNode)
        var result = classGenerate.parseListNodeType(listNode)
        assertThat(result["name"]).isEqualTo(
            ListNodeType(
                ValueType.STRING,
                canNull = true,
                isList = false
            )
        )
        assertThat(result["id"]).isEqualTo(
            ListNodeType(
                ValueType.LONG,
                canNull = true,
                isList = false
            )
        )
        assertThat(result["basicList"]).isEqualTo(
            ListNodeType(
                ValueType.LONG,
                canNull = true,
                isList = true
            )
        )
        assertThat(result["emptyList"]).isEqualTo(ListNodeType(Unit, canNull = true, isList = true))
        assertThat(result["subObject"]).isEqualTo(
            ListNodeType(
                mapOf(
                    "id" to ListNodeType(ValueType.INT, canNull = true, isList = false),
                    "name" to ListNodeType(ValueType.STRING, canNull = true, isList = false),
                    "desc" to ListNodeType(ValueType.STRING, canNull = true, isList = false),
                ), canNull = true, isList = false
            )
        )
        assertThat(result["childList"]).isEqualTo(
            ListNodeType(
                mapOf(
                    "id" to ListNodeType(ValueType.INT, canNull = false, isList = false),
                    "name" to ListNodeType(ValueType.STRING, canNull = true, isList = false),
                    "desc" to ListNodeType(ValueType.STRING, canNull = true, isList = false),
                ), canNull = true, isList = true
            )
        )


        classGenerate =
            createClassGenerate(objNode, unstableArray = false)
        result = classGenerate.parseListNodeType(listNode)
        assertThat(result["name"]).isEqualTo(
            ListNodeType(
                ValueType.STRING,
                canNull = false,
                isList = false
            )
        )
        assertThat(result["id"]).isNull()
        assertThat(result["basicList"]).isEqualTo(
            ListNodeType(
                ValueType.INT,
                canNull = false,
                isList = true
            )
        )
        assertThat(result["emptyList"]).isEqualTo(ListNodeType(Unit, canNull = false, isList = true))
        assertThat(result["subObject"]).isEqualTo(
            ListNodeType(
                mapOf(
                    "id" to ListNodeType(ValueType.INT, canNull = false, isList = false),
                    "name" to ListNodeType(ValueType.STRING, canNull = false, isList = false),
                    "desc" to ListNodeType(Unit, canNull = true, isList = false),
                ), canNull = false, isList = false
            )
        )
        assertThat(result["childList"]).isEqualTo(
            ListNodeType(
                mapOf(
                    "id" to ListNodeType(ValueType.INT, canNull = false, isList = false),
                ), canNull = false, isList = true
            )
        )
    }


    @Test
    fun testListFirstNull(){
        val listNode = Node.ListNode(
            listOf(
                Node.ObjectNode(mapOf(
                    "list" to Node.ListNode(listOf(null), ""),

                ),""),
            ), "" )
        val classGenerate =
            createClassGenerate(Node.ObjectNode(mapOf("list" to listNode), ""))
        try {
            classGenerate.parseListNodeType(listNode)
            error("test fail")
        }catch (e:Exception){
            assertThat(e.message).isEqualTo("not support array first child is null")
        }
    }

    @Test
    fun testListList(){
        val listNode = Node.ListNode(
            listOf(
                Node.ObjectNode(mapOf(
                    "list" to Node.ListNode(listOf(
                        Node.ListNode(listOf(),"")
                    ), ""),

                    ),""),
            ), "" )
        val classGenerate =
            createClassGenerate(Node.ObjectNode(mapOf("list" to listNode), ""))
        try {
            classGenerate.parseListNodeType(listNode)
            error("test fail")
        }catch (e:Exception){
            assertThat(e.message).isEqualTo("not support array in array")
        }
    }


    @Test
    fun testTypeChange(){
        val listNode = Node.ListNode(
            listOf(
               Node.ObjectNode(mapOf("name" to Node.ValueNode("kotlin", "")),""),
               Node.ObjectNode(mapOf("name" to Node.ValueNode(1, "")),""),
            ), "" )
        val classGenerate =
            createClassGenerate(Node.ObjectNode(mapOf("list" to listNode), ""))
        try {
            classGenerate.parseListNodeType(listNode)
            error("test fail")
        }catch (e:Exception){
            assertThat(e.message).isEqualTo("not support type change")
        }
    }


    private fun createClassGenerate(objNode: Node.ObjectNode, unstableArray:Boolean = true) = TestClassGenerateImpl("com.configweaver.test", "", objNode, "TestClass", unstableArray)
}