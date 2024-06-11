package com.location.configgen.core.codeGen

import com.location.configgen.core.datanode.Node
import com.location.configgen.core.datanode.nodeType
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
abstract class FileCreate(
    protected val packageName:String,
    protected val outputDir: String,
    protected val json: String,
    protected val className: String,
) {

    fun create(){
        val jsonParser = JSONParser()
        val jsPoint = jsonParser.parse(json)
        val classSpec = createTypeSpecBuilder(className).apply {
            addJavaDoc("RawJson:$json")
        }
    }



    abstract fun createTypeSpecBuilder(className: String): TypeSpecBuilderWrapper


    private fun parseJsonObj(typeSpecBuilder: TypeSpecBuilderWrapper, jsObj: JSONObject){

        jsObj.forEach { k, v ->
            when (v) {
                is JSONObject -> {
                    val innerClass = createTypeSpecBuilder(k.toString().className).apply {
                        addJavaDoc("key:$k - value:$v")
                    }
                    parseJsonObj(innerClass, v)
                    typeSpecBuilder.addType(innerClass.build())
                }

                is JSONArray -> {

                }

                else -> {
                    addStaticFiled(typeSpecBuilder, k.toString(), v)
                }

            }
        }
    }


    private fun parseJsonNode(jsObj: JSONObject): Node {
        val node = Node.ObjectNode(className, mutableMapOf())
        jsObj.forEach { k, v ->
            if(v == null){
                return@forEach
            }
            when (v) {
                is JSONObject -> {
                    val childNode = parseJsonNode(v)
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

    abstract fun addStaticFiled(typeSpecBuilder: TypeSpecBuilderWrapper, key: String, v: Any)

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