package com.location.configmerge.codeGen

import com.location.configmerge.datanode.Node
import com.location.configmerge.datanode.nodeType
import com.squareup.kotlinpoet.TypeSpec
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import java.util.Locale

/**
 *
 * @author tianxiaolong
 * time：2024/6/7 17:19
 * description：
 */
class FileCreate(
    protected val packageName:String,
    protected val outputDir: String,
    protected val json: String,
    protected val className: String,
) {

    fun create(){
        val jsonParser = JSONParser()
        val jsPoint = jsonParser.parse(json)

        val node = parseJsonObj(jsPoint as JSONObject)
        println("node = $node")

    }

    private fun parseJsonObj(jsObj: JSONObject): Node {
        val node = Node.ObjectNode(className, mutableMapOf())
        jsObj.forEach { k, v ->
            when (v) {
                is JSONObject -> {
                    val childNode = parseJsonObj(v)
                    node.property[k.toString().fieldName] = childNode
                }

                is JSONArray -> {

                }

                else -> {
                    node.property[k.toString().fieldName] = Node.ValueNode(v, v.nodeType)
                }
            }
        }

        return node

    }




    protected val currentTime:String
        get() {
            val format = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
            return format.format(java.util.Date())
        }



}

private val String.fieldName
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