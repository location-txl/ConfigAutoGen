package com.location.configgen.core.codeGen

import com.location.configgen.core.datanode.Node
import com.location.configgen.core.datanode.ValueType
import com.location.configgen.core.datanode.nodeType
import com.location.configgen.core.datanode.valueType
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
abstract class FileCreate<T : TypeSpecBuilderWrapper>(
    protected val packageName:String,
    protected val outputDir: String,
    protected val json: String,
    protected val className: String,
    /**
     * 是否生成不稳定的数组 如果为 true 则会扫描整个数组的所有元素 生产一个最全的Object
     */
    private val unstableArray:Boolean = true
) {

    fun create(){
        val jsonParser = JSONParser()
        val jsPoint = jsonParser.parse(json)
        val classSpec = createTypeSpecBuilder(className, false).apply {
            addJavaDoc("RawJson:$json")
        }
        parseJsonObj(classSpec, jsPoint as JSONObject)
        writeFile("""
             config-merge plugin automatically generated, please do not modify
                time:$currentTime
                author:tianxiaolong
                Version:$GENERATE_VERSION
                SourceCode:gitLab/RobotUI/config-merge
        """.trimIndent(), classSpec)
    }
    abstract fun writeFile(
        fileComment:String,
        classSpec:T)



    abstract fun createTypeSpecBuilder(className: String, isInner:Boolean): T


    private fun parseJsonObj(typeSpecBuilder: T, jsObj: JSONObject){

        jsObj.forEach { k, v ->
            when (v) {
                is JSONObject -> {
                    val innerClass = createTypeSpecBuilder(k.toString().className, true).apply {
                        addJavaDoc("key:$k - value:$v")
                    }
                    parseJsonObj(innerClass, v)
                    typeSpecBuilder.addType(innerClass.build())
                }

                is JSONArray -> {
                    if(v.isNotEmpty()){
                        parseJsonArray(typeSpecBuilder, k.toString(), v, "$packageName.$className")
                    }else{
                        println("$k is empty array")
                    }
                }

                else -> {
                    addStaticFiled(typeSpecBuilder, k.toString(), v)
                }

            }
        }
    }


    private fun parseJsArrayType(jsArray: JSONArray):Map<String,JsArrayType>{
        val filedMap = mutableMapOf<String,JsArrayType>()
        var index = 0
        do {
            val obj = jsArray[index++] as JSONObject
            obj.forEach { k, v ->
                val oldType = filedMap[k]
                when(v){
                    is JSONArray -> {
                        if (oldType == null && v.isEmpty()) {
                            filedMap[k.toString()] = JsArrayType(Unit, false)
                        } else if(v.isNotEmpty()) {
                            filedMap[k.toString()] =
                                oldType?.copy(type = parseJsArrayType(v)) ?: JsArrayType(
                                    parseJsArrayType(v), false
                                )
                        }
                    }
                    else -> {
                        if(oldType == null){
                            filedMap[k.toString()] = JsArrayType(v?.valueType ?: Unit, v == null)
                        }else{
                            if(v == null){
                                filedMap[k.toString()] = oldType.copy(isNull = true)
                            }else {
                                val valueType = v.valueType
                                if(oldType.type.javaClass == valueType.javaClass) {
                                    val oldValueType = oldType.type as ValueType
                                    if(oldValueType.groupId == valueType.groupId && oldValueType.ordinal < valueType.ordinal){
                                        //可以向上提升
                                        filedMap[k.toString()] = oldType.copy(type = valueType)
                                    }
                                }else{
                                    //直接替换类型
                                    filedMap[k.toString()] = oldType.copy(type = valueType)
                                }
                            }
                        }
                    }
                }


            }
        }while (unstableArray && index < jsArray.size )
        return filedMap
    }

    private fun parseJsonArray(typeSpecBuilder: T, key:String, jsArray:JSONArray, innerPkgName:String){
        when(jsArray[0]){
            is JSONObject -> {
                parseJsArrayType(jsArray).also {
                    println("parseJsonArray obj:$it")
                }
//                val typeObject = if(unstableArray){
//
//                }else{
//
//                }
//                val innerClass = createTypeSpecBuilder(key.className, true).apply {
//                    addJavaDoc("key:$key - value:$jsArray")
//                }
//                parseJsonObj(innerClass, jsArray[0] as JSONObject)
//                typeSpecBuilder.addType(innerClass.build())
            }
            is JSONArray -> {
                println("not support array in array")
            }
            else -> {
                addNormalArray(typeSpecBuilder, key, jsArray)
            }
        }
    }


    private fun parseJsObjToType(obj:JSONObject){

    }

    protected abstract fun addNormalArray(
        typeSpecBuilder: T,
        key: String,
        jsArray: JSONArray
    )


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

    abstract fun addStaticFiled(typeSpecBuilder: T, key: String, v: Any)

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